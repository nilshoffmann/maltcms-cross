/* Cross, common runtime object support system.
 * Copyright (C) 2008-2012, The authors of Cross. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Cross may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Cross, you may choose which license to receive the code
 * under. Certain files or entire directories may not be covered by this
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a
 * LICENSE file in the relevant directories.
 *
 * Cross is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package cross.datastructures.pipeline;

import cross.annotations.AnnotationInspector;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.tools.FileTools;
import cross.datastructures.tuple.TupleND;
import cross.datastructures.workflow.IWorkflow;
import cross.exception.ConstraintViolationException;
import cross.exception.ExitVmException;
import cross.tools.PublicMemberGetters;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.mpaxs.api.ConfigurationKeys;
import net.sf.mpaxs.api.ExecutionType;
import net.sf.mpaxs.spi.concurrent.ComputeServerFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Extension of CommandPipeline that knows how to skip existing results.
 *
 * @author Nils Hoffmann
 * @see ICommandSequence
 */
@Slf4j
@Data
@ServiceProvider(service = ICommandSequence.class, position = Integer.MIN_VALUE + 1)
public class ResultAwareCommandPipeline extends CommandPipeline {

//    @Getter(AccessLevel.PROTECTED)
//    @Setter(AccessLevel.PROTECTED)
//    private boolean loadedPreviousWorkflowResults = false;
    /**
     * Create a new command pipeline instance.
     */
    public ResultAwareCommandPipeline() {
    }

    private PropertiesConfiguration hashes;

    protected Collection<File> getInputFiles(TupleND<IFileFragment> inputFiles) {
        List<File> inputFileList = new ArrayList<File>();
        for (IFileFragment f : inputFiles) {
            try {
                inputFileList.add(FileTools.getFile(f));
            } catch (IOException ioex) {
                throw new ConstraintViolationException(ioex);
            }
        }
        return inputFileList;
    }

    protected PropertiesConfiguration getHashes(IWorkflow workflow) {
        File hashesFile = getHashesFile(workflow);
        if (hashes == null) {
            try {
                hashes = new PropertiesConfiguration(hashesFile);
                hashes.setAutoSave(true);
                hashes.setReloadingStrategy(new FileChangedReloadingStrategy());
                log.debug("Hashes: {}", ConfigurationUtils.toString(hashes));
                return hashes;
            } catch (ConfigurationException ex) {
                log.error("Could not load configuration at " + hashesFile, ex);
                throw new ExitVmException(ex);
            }
        } else {
            return hashes;
        }
    }

    protected File getHashesFile(IWorkflow workflow) {
        return new File(workflow.getOutputDirectory(), ".hashes");
    }

    protected void updateHashes(TupleND<IFileFragment> inputFiles, IFragmentCommand cmd) {
        PropertiesConfiguration pc = getHashes(cmd.getWorkflow());
        Collection<File> files = getInputFiles(inputFiles);
        files.add(cmd.getWorkflow().getOutputDirectory(cmd));
        String fileHash = getRecursiveFileHash(files);
        String parametersHash = getParameterHash(cmd);
        pc.setProperty(getFileHashKey(cmd), fileHash);
        pc.setProperty(getParametersHashKey(cmd), parametersHash);
    }

    protected String getFileHashKey(IFragmentCommand cmd) {
        URI fragmentCommandOutputDirectory = cmd.getWorkflow().getOutputDirectory(cmd).getAbsoluteFile().toURI();
        URI relativeFile = FileTools.getRelativeUri(cmd.getWorkflow().getOutputDirectory().getAbsoluteFile().toURI(), fragmentCommandOutputDirectory);
        return cmd.getClass().getName() + "-" + relativeFile.getPath() + ".fileHash";
    }

    protected String getParametersHashKey(IFragmentCommand cmd) {
        URI fragmentCommandOutputDirectory = cmd.getWorkflow().getOutputDirectory(cmd).getAbsoluteFile().toURI();
        URI relativeFile = FileTools.getRelativeUri(cmd.getWorkflow().getOutputDirectory().getAbsoluteFile().toURI(), fragmentCommandOutputDirectory);
        return cmd.getClass().getName() + "-" + relativeFile.getPath() + ".parametersHash";
    }

    protected boolean isInputUpToDate(TupleND<IFileFragment> inputFiles, IWorkflow workflow) {
        PropertiesConfiguration pc = getHashes(workflow);
        Collection<File> files = getInputFiles(inputFiles);
        String oldWorkflowInputFilesHash = pc.getString(workflow.getName() + ".inputFiles.fileHash", "");
        String workflowInputFilesHash = getRecursiveFileHash(files);
        return oldWorkflowInputFilesHash.equals(workflowInputFilesHash);
    }

    protected void updateWorkflowInputHashes(TupleND<IFileFragment> inputFiles, IWorkflow workflow) {
        PropertiesConfiguration pc = getHashes(workflow);
        Collection<File> files = getInputFiles(inputFiles);
        String fileHash = getRecursiveFileHash(files);
        pc.setProperty(workflow.getName() + ".inputFiles.fileHash", fileHash);
    }

    protected boolean isUpToDate(TupleND<IFileFragment> inputFiles, IFragmentCommand cmd) {
        PropertiesConfiguration pc = getHashes(cmd.getWorkflow());
        String oldOutputDirHash = pc.getString(getFileHashKey(cmd), "");
        Collection<File> files = getInputFiles(inputFiles);
        files.add(cmd.getWorkflow().getOutputDirectory(cmd));
        log.debug("Files to hash: {}", files);
        String outputDirHash = getRecursiveFileHash(files);
        log.debug("Old file hash: {} New file hash: {}", oldOutputDirHash, outputDirHash);
        boolean outputDirHashesEqual = oldOutputDirHash.equals(outputDirHash);
        String oldParameterHash = pc.getString(getParametersHashKey(cmd), "");
        String parameterHash = getParameterHash(cmd);
        log.debug("Old parameter hash: {} New parameter hash: {}", oldParameterHash, parameterHash);
        log.debug("Is up to date: {}: {}", cmd.getClass().getName(), outputDirHashesEqual && oldParameterHash.equals(parameterHash));
        return outputDirHashesEqual && oldParameterHash.equals(parameterHash);
    }

    /**
     * Used to check, whether any files have changed compared to the last
     * invocation.
     *
     * @return
     */
    protected String getRecursiveFileHash(Collection<File> inputFiles) {
        //list files below input and output
        SortedSet<File> files = new TreeSet<File>();
        for (File file : inputFiles) {
            if (file.isDirectory()) {
                files.addAll(FileUtils.listFiles(file, new String[]{"*"}, true));
            } else {
                files.add(file);
            }
        }
        return digest(files);
    }

    /**
     * Calculates a byte-level digest of the given files.
     *
     * @param files the files to calculate the digest for.
     * @return the hexadecimal, zero-padded digest, or null if any exceptions
     *         occurred
     */
    public String digest(Collection<File> files) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            for (File file : files) {
                try (InputStream is = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
                    byte[] buffer = new byte[8192];
                    int read = 0;
                    while ((read = is.read(buffer)) > 0) {
                        digest.update(buffer, 0, read);
                    }
                } catch (IOException ioex) {
                    Logger.getLogger(ResultAwareCommandPipeline.class.getName()).log(Level.SEVERE, null, ioex);
                    return null;
                }
            }
            byte[] sha1 = digest.digest();
            BigInteger bigInt = new BigInteger(1, sha1);
            return StringUtils.leftPad(bigInt.toString(16), 40, "0");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ResultAwareCommandPipeline.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Used to check, whether any parameters have changed compared to the last
     * invocation.
     *
     * @param cmd the command to calculate a hash code for, based on reflection
     * @return the hash code as string
     */
    protected String getParameterHash(IFragmentCommand cmd) {
        Collection<String> fieldNames = AnnotationInspector.getRequiredConfigFieldNames(cmd.getClass());
        PublicMemberGetters<IFragmentCommand> pmg = new PublicMemberGetters<IFragmentCommand>(cmd);
        HashCodeBuilder hcb = new HashCodeBuilder();
        for (String fieldName : fieldNames) {
            Method m = pmg.getMethodForFieldName(fieldName);
            if (m != null) {
                try {
                    Object o = m.invoke(cmd);
                    hcb.append(o);
                    log.debug("Accessing field {} of {}. Current hash={}", new Object[]{fieldName, cmd.getClass().getName(), hcb.toHashCode()});
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ResultAwareCommandPipeline.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(ResultAwareCommandPipeline.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(ResultAwareCommandPipeline.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return hcb.toHashCode() + "";
    }

    @Override
    protected void runFragmentCommand(IWorkflow workflow, IFragmentCommand cmd) {
        try {
            beforeCommand(cmd);
            TupleND<IFileFragment> results;
            if (!isUpToDate(getTmp(), cmd)) {
                if (getWorkflow().getOutputDirectory(cmd).exists() && getWorkflow().getOutputDirectory(cmd).listFiles().length > 0) {
                    log.info("Deleting invalid results for {} below {}", cmd, getWorkflow().getOutputDirectory(cmd));
                    try {
                        FileUtils.deleteDirectory(getWorkflow().getOutputDirectory(cmd));
                    } catch (IOException ex) {
                        log.warn("Caught IO Exception while trying to delete workflow output directory at " + getWorkflow().getOutputDirectory());
                        throw new RuntimeException(ex);
                    }
                }
                long start = System.nanoTime();
                results = cmd.apply(getTmp());
                storeCommandRuntime(start, System.nanoTime(), cmd, getWorkflow());
            } else {
                log.info("Skipping, everything up to date!");
                File outputDir = getWorkflow().getOutputDirectory(cmd);
                Collection<File> inputFiles = FileUtils.listFiles(outputDir, new String[]{"cdf", "CDF", "nc", "NC"}, false);
                TupleND<IFileFragment> inputFragments = new TupleND<>();
                for (File f : inputFiles) {
                    inputFragments.add(getWorkflow().getFactory().getFileFragmentFactory().create(f));
                }
                log.debug("Setting file fragments {} as next input!", inputFragments);
                results = inputFragments;
            }
            updateHashes(getTmp(), cmd);
            setTmp(results);
            log.debug("Hashes: {}", ConfigurationUtils.toString(getHashes()));
        } finally {
            afterCommand(cmd);
        }
    }

    @Override
    public void before() {
        log.info("Looking for results from previous workflow invocation...");
        if (getWorkflow().getWorkflowXmlFile().isFile()) {
            log.info("Found previous workflow.xml file!");
            if (isInputUpToDate(getInput(), getWorkflow())) {
                log.info("Input data has not changed, restoring workflow!");
                getWorkflow().load(getWorkflow().getWorkflowXmlFile());
            } else {
                try {
                    log.info("Input data has changed, deleting previous workflow output!");
                    FileUtils.deleteDirectory(getWorkflow().getOutputDirectory());
                    getWorkflow().clearResults();
                } catch (IOException ex) {
                    throw new RuntimeException(
                        "Deletion of directory " + getWorkflow().getOutputDirectory() + " failed!",
                        ex);
                }
                getWorkflow().getOutputDirectory().mkdirs();
            }
        } else {
            log.info("Did not find results from a previous workflow instance!");
        }
        //update input file hashes
        updateWorkflowInputHashes(getInput(), getWorkflow());
        if (getExecutionServer() == null && !getWorkflow().isExecuteLocal()) {
            log.info("Launching execution infrastructure!");
            setExecutionServer(ComputeServerFactory.getComputeServer());
            File computeHostJarLocation = new File(System.getProperty("maltcms.home"), "maltcms.jar");
            if (!computeHostJarLocation.exists() || !computeHostJarLocation.isFile()) {
                throw new ExitVmException("Could not locate maltcms.jar in " + System.getProperty("maltcms.home"));
            }
            final PropertiesConfiguration cfg = new PropertiesConfiguration();
//set execution type
            cfg.setProperty(ConfigurationKeys.KEY_EXECUTION_MODE, ExecutionType.DRMAA);
//set location of compute host jar
            cfg.setProperty(ConfigurationKeys.KEY_PATH_TO_COMPUTEHOST_JAR, computeHostJarLocation);
//exit to console when master server shuts down
            cfg.setProperty(ConfigurationKeys.KEY_MASTER_SERVER_EXIT_ON_SHUTDOWN, true);
//limit the number of used compute hosts
            cfg.setProperty(ConfigurationKeys.KEY_MAX_NUMBER_OF_CHOSTS, getWorkflow().getConfiguration().getInt("maltcms.pipelinethreads", 1));
//native specs for the drmaa api
            cfg.setProperty(ConfigurationKeys.KEY_NATIVE_SPEC, getWorkflow().getConfiguration().getString("mpaxs.nativeSpec", ""));
            getExecutionServer().startMasterServer(cfg);
        }
    }

}
