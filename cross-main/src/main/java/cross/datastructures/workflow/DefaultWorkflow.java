/*
 * Cross, common runtime object support system.
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
package cross.datastructures.workflow;

import cross.IFactory;
import cross.annotations.Configurable;
import cross.commands.fragments.AFragmentCommand;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.FileFragment;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.pipeline.ICommandSequence;
import cross.datastructures.tuple.TupleND;
import cross.event.AEvent;
import cross.event.EventSource;
import cross.event.IEvent;
import cross.event.IEventSource;
import cross.event.IListener;
import cross.exception.ConstraintViolationException;
import cross.io.misc.DefaultConfigurableFileFilter;
import cross.io.xml.IXMLSerializable;
import cross.tools.StringTools;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openide.util.lookup.ServiceProvider;

/**
 * A default implementation of {@link cross.datastructures.workflow.IWorkflow}.
 * Is a source of
 * <code>IEvent<IWorkflowResult></code> events.
 *
 * @author Nils Hoffmann
 *
 */
@Slf4j
@ServiceProvider(service = IWorkflow.class)
public class DefaultWorkflow implements IWorkflow, IXMLSerializable {

    /**
     *
     */
    private static final long serialVersionUID = 1781229121330043626L;
    private Collection<IWorkflowResult> al = new ArrayList<>();
    private transient IEventSource<IWorkflowResult> iwres = new EventSource<>();
    private ICommandSequence commandSequence = null;
    private String name = "workflow";
    private IFragmentCommand activeCommand = null;
    @Configurable
    private String xslPathPrefix;
    @Configurable(name = "resultFileFilter")
    private String fileFilter = DefaultConfigurableFileFilter.class.getName();
    @Configurable
    private boolean saveHTML = false;
    @Configurable
    private boolean saveTEXT = false;
    private Date date = new Date();
    private Configuration cfg = new PropertiesConfiguration();
    private boolean executeLocal = true;
    private File outputDirectory = new File(System.getProperty("user.dir"));
    private List<IWorkflowPostProcessor> workflowPostProcessors = new ArrayList<>();
    private transient IFactory factory;

    @Override
    public void addListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.iwres.addListener(l);
    }

    @Override
    public void append(final IWorkflowResult iwr) {
        if (this.al == null) {
            this.al = new LinkedHashSet<>();
        }
        if (iwr instanceof IWorkflowProgressResult) {
            final IWorkflowProgressResult iwpr = (IWorkflowProgressResult) iwr;
            log.info("Step {}/{}, Overall progress: {}%", new Object[]{
                iwpr.getCurrentStep(), iwpr.getNumberOfSteps(),
                iwpr.getOverallProgress()});
        } else {
            this.al.add(iwr);
        }
        fireEvent(new AEvent<>(iwr, DefaultWorkflow.this.iwres));
    }

    @Override
    public void appendXML(final Element e) {
        log.debug("Appending xml for DefaultWorkflow " + getName());
        getCommandSequence().appendXML(e);
        for (final IWorkflowResult wr : this.al) {
            wr.appendXML(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.IConfigurable#configure(org.apache.commons.configuration.Configuration
     * )
     */
    @Override
    public void configure(final Configuration cfg) {
        this.saveHTML = cfg.getBoolean(this.getClass().getName() + ".saveHTML",
            false);
        this.saveTEXT = cfg.getBoolean(this.getClass().getName() + ".saveTEXT",
            false);
        this.xslPathPrefix = cfg.getString(this.getClass().getName()
            + ".xslPathPrefix", "");
        this.fileFilter = cfg.getString(this.getClass().getName()
            + ".resultFileFilter", DefaultConfigurableFileFilter.class.
            getName());
    }

    @Override
    public void fireEvent(final IEvent<IWorkflowResult> e) {
        this.iwres.fireEvent(e);
    }

    /**
     * @return the ICommandSequence
     */
    @Override
    public ICommandSequence getCommandSequence() {
        return this.commandSequence;
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflow#getConfiguration()
     */
    @Override
    public Configuration getConfiguration() {
        return this.cfg;
    }

    /**
     * @return the fileFilter
     */
    public String getFileFilter() {
        return this.fileFilter;
    }

    /**
     * @return the iwres
     */
    public IEventSource<IWorkflowResult> getIwres() {
        return this.iwres;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Iterator<IWorkflowResult> getResults() {
        return this.al.iterator();
    }

    /*
     * (non-Javadoc)
     *
     * @see cross.datastructures.workflow.IWorkflow#getStartupDate()
     */
    @Override
    public Date getStartupDate() {
        return this.date;
    }

    /**
     * @return the IWorkflowResults as List
     */
    public Collection<IWorkflowResult> getWorkflowResults() {
        return this.al;
    }

    /**
     * @return the xslPathPrefix
     */
    public String getXslPathPrefix() {
        return this.xslPathPrefix;
    }

    @Override
    public void readXML(final Element e) throws IOException,
        ClassNotFoundException {
        final String workflowname = e.getAttributeValue("name");
        if (workflowname != null) {
            setName(workflowname);
        }

        TupleND<IFileFragment> inputFragments = new TupleND<>();
        Element workflowInputs = e.getChild("workflowInputs");
        for (Object child : workflowInputs.getChildren("workflowInput")) {
            Element workflowInput = (Element) child;
            String uri = workflowInput.getAttributeValue("uri");
            inputFragments.add(new FileFragment(URI.create(uri)));
        }

        Element workflowOutputs = e.getChild("workflowOutputs");
        for (Object child : workflowOutputs.getChildren("workflowOutput")) {
            Element workflowOutput = (Element) child;
            String uri = workflowOutput.getAttributeValue("uri");
        }

        Element workflowCommands = e.getChild("workflowCommands");
        List<?> commandChildren = workflowCommands.getChildren("workflowCommand");
        if (commandChildren.size() != getCommandSequence().getCommands().size()) {
            throw new ConstraintViolationException("Number of workflow commands between stored and active workflow differ! Not loading old workflow!");
        }
        int i = 0;
        for (Object child : commandChildren) {
            Element workflowCommand = (Element) child;
            String clazz = workflowCommand.getAttributeValue("class");
            if (!getCommandSequence().getCommands().get(i).getClass().getCanonicalName().equals(clazz)) {
                throw new ConstraintViolationException("Workflow commands differ between stored and active workflow at position " + i + "!");
            }
            i++;
        }

        final List<?> l = e.getChildren("workflowElementResult");
        for (final Object obj : l) {
            final Element elem = (Element) obj;
            final String cls = elem.getAttributeValue("class");
            final String slot = elem.getAttributeValue("slot");
            final String generator = elem.getAttributeValue("generator");
            IWorkflowResult iwr = null;
            String file = "";
            try {

                iwr = getFactory().getObjectFactory().instantiate(cls,
                    IWorkflowResult.class);
                final IWorkflowElement iwe = getFactory().getObjectFactory().
                    instantiate(generator,
                        IWorkflowElement.class);
                iwr.setWorkflowElement(iwe);
                iwr.setWorkflowSlot(WorkflowSlot.valueOf(slot));
                if (iwr instanceof IWorkflowFileResult) {
                    file = elem.getAttributeValue("file");
                    IWorkflowFileResult fileResult = (IWorkflowFileResult) iwr;
                    fileResult.setFile(new File(file));
                    Element resources = e.getChild("resources");
                    if (resources != null) {
                        List<IFileFragment> fragments = new ArrayList<>();
                        for (Object o : resources.getChildren("resource")) {
                            Element resource = (Element) o;
                            URI uri = URI.create(resource.getAttributeValue("resource"));
                            fragments.add(new FileFragment(uri));
                        }
                        fileResult.setResources(fragments.toArray(new IFileFragment[fragments.size()]));
                    }
                } else {
                    log.warn("Unsupported workflow result type! Can not convert!");
                }
                append(iwr);
            } catch (ConstraintViolationException rnae) {
                if (iwr != null) {
                    log.warn("Could not load result! Not found at location: " + file);
                } else {
                    log.warn("Could not load workflow result!");
                }
            }
        }
    }

    @Override
    public void removeListener(final IListener<IEvent<IWorkflowResult>> l) {
        this.iwres.removeListener(l);
    }

    @Override
    public void load(File f) {
        if (f.exists()) {
            try {
                SAXBuilder saxBuilder = new SAXBuilder();
                Document dom = saxBuilder.build(f);
                try {
                    log.info("Reading workflow state from {}.", f);
                    readXML(dom.getRootElement());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(DefaultWorkflow.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (JDOMException | IOException ex) {
                Logger.getLogger(DefaultWorkflow.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            log.info("No workflow.xml from previous invocation found!");
        }
    }

    @Override
    public void save() {
        try {
            final String wflname = getName();
            log.info("Saving workflow {}", wflname);
            final Document doc = new Document();
            final ProcessingInstruction pi = new ProcessingInstruction(
                "xml-stylesheet",
                "type=\"text/xsl\" href=\"http://maltcms.sourceforge.net/res/maltcmsHTMLResult.xsl\"");
            doc.addContent(pi);
            doc.addContent(writeXML());
            final XMLOutputter outp = new XMLOutputter(Format.getPrettyFormat());
            try {
                final File f = new File(getOutputDirectory(), getName() + ".xml");//new File(wflname);
                final File dir = f.getParentFile();
                dir.mkdirs();
                f.createNewFile();
                outp.output(doc, new BufferedOutputStream(new FileOutputStream(
                    f)));
                if (this.saveHTML) {
                    saveHTML(f);
                }
                if (this.saveTEXT) {
                    saveTEXT(f);
                }
            } catch (final IOException e) {
                log.error(e.getLocalizedMessage());
            }
        } catch (final FileNotFoundException e) {
            log.error(e.getLocalizedMessage());
        } catch (final IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * Applies a stylesheet transformation to this DefaultWorkflow to create
     * html with clickable links.
     *
     * @param f
     */
    protected void saveHTML(final File f) {

        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final InputStream xsstyler = getClass().getClassLoader().
                getResourceAsStream("res/xslt/maltcmsHTMLResult.xsl");
            final StreamSource sstyles = new StreamSource(xsstyler);
            final FileInputStream xsr = new FileInputStream(f);
            final FileOutputStream xsw = new FileOutputStream(new File(f.
                getParentFile(), "workflow.html"));
            final StreamSource sts = new StreamSource(xsr);
            final StreamResult str = new StreamResult(xsw);
            final Transformer t = tf.newTransformer(sstyles);
            t.transform(sts, str);
        } catch (final TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final FileNotFoundException | TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Applies a stylesheet transformation to this DefaultWorkflow to create
     * text.
     *
     * @param f
     */
    protected void saveTEXT(final File f) {

        final TransformerFactory tf = TransformerFactory.newInstance();
        try {
            final InputStream xsstyler = getClass().getClassLoader().
                getResourceAsStream("res/xslt/maltcmsTEXTResult.xsl");
            final StreamSource sstyles = new StreamSource(xsstyler);
            final FileInputStream xsr = new FileInputStream(f);
            final FileOutputStream xsw = new FileOutputStream(new File(f.
                getParentFile(), "workflow.txt"));
            final StreamSource sts = new StreamSource(xsr);
            final StreamResult str = new StreamResult(xsw);
            final Transformer t = tf.newTransformer(sstyles);
            t.transform(sts, str);
        } catch (final TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final FileNotFoundException | TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     */
    @Override
    public void setCommandSequence(final ICommandSequence ics) {
        this.commandSequence = ics;
        this.commandSequence.setWorkflow(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#setConfiguration(org.apache.commons
     * .configuration.Configuration)
     */
    @Override
    public void setConfiguration(final Configuration configuration) {
        this.cfg = configuration;

    }

    /**
     * @param iwres the EventSource<IWorkflowResult> to set
     */
    public void setEventSource(final IEventSource<IWorkflowResult> iwres) {
        this.iwres = iwres;
    }

    /**
     * @param fileFilter the fileFilter to set
     */
    public void setFileFilter(final String fileFilter) {
        this.fileFilter = fileFilter;
    }

    @Override
    public void setName(final String name1) {
        this.name = name1;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#setStartupDate(java.util.Date)
     */
    @Override
    public void setStartupDate(final Date date1) {
        this.date = date1;
    }

    /**
     * @param xslPathPrefix the xslPathPrefix to set
     */
    public void setXslPathPrefix(final String xslPathPrefix) {
        this.xslPathPrefix = xslPathPrefix;
    }

    @Override
    public Element writeXML() throws IOException {
        final Element root = new Element("workflow");
        root.setAttribute("class", getClass().getCanonicalName());
        root.setAttribute("name", getName());
        appendXML(root);
        return root;
    }

    /*
     * (non-Javadoc)
     *
     * @seecross.datastructures.workflow.IWorkflow#getOutputDirectory(java.lang.
     * Object)
     */
    @Override
    public File getOutputDirectory(Object iwe) {
        if (iwe instanceof AFragmentCommand) {
            Collection<IFragmentCommand> c = this.commandSequence.getCommands();
            int i = 0;
            int digits = (int) Math.ceil(Math.log10(c.size())) + 1;
            for (IFragmentCommand afc : c) {
                IFragmentCommand iwa = (IFragmentCommand) iwe;
                log.debug("Checking for reference equality!");
                // check for reference equality
                if (iwa == afc) {
                    log.debug("Reference equality holds!");
                    if (activeCommand == null) {
                        activeCommand = iwa;
                    }
                    if (activeCommand != iwa) {
                        activeCommand = iwa;
                    }
                    File outputFile = new File(
                        outputDirectory, String.format(
                            "%0" + digits + "d", i)
                        + "_" + iwa.getClass().getSimpleName());
                    outputFile.mkdirs();
                    log.info("Output dir for object of type {}: {}", iwe.getClass().getSimpleName(), this.outputDirectory);
                    return outputFile;
                }
                i++;
            }
            if (activeCommand != null) {
                File dir = new File(getOutputDirectory(activeCommand), iwe.
                    getClass().getSimpleName());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                log.info("Output dir for object of type {}: {}", iwe.getClass().getSimpleName(), this.outputDirectory);
                return dir;
            }
        } else if (iwe instanceof IWorkflowElement) {
            if (activeCommand != null) {
                File dir = new File(getOutputDirectory(activeCommand), iwe.
                    getClass().getSimpleName());
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                log.info("Output dir for object of type {}: {}", iwe.getClass().getSimpleName(), this.outputDirectory);
                return dir;
            }
        }

        File outputFile = new File(outputDirectory,
            iwe.getClass().getSimpleName());
        outputFile.mkdirs();
        log.info("Output dir for object of type {}: {}", iwe.getClass().getSimpleName(), this.outputDirectory);
        return outputFile;
    }

    @Override
    public File getWorkflowXmlFile() {
        File f = new File(getOutputDirectory(), getName() + ".xml");
        return f;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsFor(cross.datastructures
     * .fragments.IFileFragment)
     */
    @Override
    public List<IWorkflowResult> getResultsFor(IFileFragment iff) {
        List<IWorkflowResult> l = new ArrayList<>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            if (iwr instanceof IWorkflowFileResult) {
                IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                IFileFragment[] res = iwfr.getResources();
                for (IFileFragment resf : res) {
                    if (StringTools.removeFileExt(resf.getName()).equals(
                        StringTools.removeFileExt(iff.getName()))) {
                        l.add(iwfr);
                    }
                }
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsFor(cross.commands.
     * fragments.IWorkflowElement)
     */
    @Override
    public List<IWorkflowResult> getResultsFor(IWorkflowElement afc) {
        List<IWorkflowResult> l = new ArrayList<>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            IWorkflowElement iwe = iwr.getWorkflowElement();
            if (iwe.getClass().getName().equals(afc.getClass().getName())) {
                l.add(iwr);
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsFor(cross.commands.
     * fragments.IWorkflowElement, cross.datastructures.fragments.IFileFragment)
     */
    @Override
    public List<IWorkflowResult> getResultsFor(IWorkflowElement afc,
        IFileFragment iff) {
        List<IWorkflowResult> afcl = getResultsFor(afc);
        List<IWorkflowResult> l = new ArrayList<>();
        for (IWorkflowResult iwr : afcl) {
            if (iwr instanceof IWorkflowFileResult) {
                IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                IFileFragment[] res = iwfr.getResources();
                for (IFileFragment resf : res) {
                    if (StringTools.removeFileExt(resf.getName()).equals(
                        StringTools.removeFileExt(iff.getName()))) {
                        l.add(iwfr);
                    }
                }
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsOfType(java.lang.String
     * )
     */
    @Override
    public List<IWorkflowResult> getResultsOfType(String fileExtension) {
        List<IWorkflowResult> l = new ArrayList<>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            if (iwr instanceof IWorkflowFileResult) {
                IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                if (iwfr.getFile().getAbsolutePath().endsWith(fileExtension)) {
                    l.add(iwr);
                }
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * cross.datastructures.workflow.IWorkflow#getResultsOfType(cross.datastructures
     * .workflow.IWorkflowElement, java.lang.String)
     */
    @Override
    public List<IWorkflowResult> getResultsOfType(IWorkflowElement afc,
        String fileExtension) {
        List<IWorkflowResult> l = new ArrayList<>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            if (iwr instanceof IWorkflowFileResult) {
                IWorkflowFileResult iwfr = (IWorkflowFileResult) iwr;
                if (iwfr.getWorkflowElement().getClass().getCanonicalName().
                    equals(afc.getClass().getCanonicalName())) {
                    if (iwfr.getFile().getAbsolutePath().endsWith(fileExtension)) {
                        l.add(iwr);
                    }
                }
            }
        }
        return l;
    }

    @Override
    public <T> List<IWorkflowObjectResult> getResultsOfType(IWorkflowElement afc,
        Class<? extends T> c) {
        List<IWorkflowObjectResult> l = new ArrayList<>();
        Iterator<IWorkflowResult> iter = getResults();
        while (iter.hasNext()) {
            IWorkflowResult iwr = iter.next();
            if (iwr instanceof IWorkflowObjectResult) {
                IWorkflowObjectResult iwfr = (IWorkflowObjectResult) iwr;
                if (iwfr.getWorkflowElement().getClass().getCanonicalName().
                    equals(afc.getClass().getCanonicalName())) {
                    if (c.isAssignableFrom(iwfr.getObject().getClass())) {
                        l.add((IWorkflowObjectResult) iwr);
                    }
                }
            }
        }
        return l;
    }

    @Override
    public boolean isExecuteLocal() {
        return executeLocal;
    }

    @Override
    public void setExecuteLocal(boolean executeLocal) {
        this.executeLocal = executeLocal;
    }

    @Override
    public TupleND<IFileFragment> call() throws Exception {
        TupleND<IFileFragment> results = null;
        if (commandSequence.isCheckCommandDependencies() && !commandSequence.validate()) {
            throw new ConstraintViolationException("Pipeline validation failed! Check output for details!");
        }
        try {
            commandSequence.before();
            while (commandSequence.hasNext()) {
                results = commandSequence.next();
            }
        } finally {
            //ensure that any resources are cleaned up
            commandSequence.after();
            // Save configuration
            getFactory().dumpConfig("runtime.properties", getStartupDate());
        }
        //only run workflow post processors if we have not experienced any exceptions
        for (IWorkflowPostProcessor pp : workflowPostProcessors) {
            log.info("Running workflowPostProcessor {}", pp.getClass().getName());
            pp.process(this);
        }
        return results;
    }

    @Override
    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public void setOutputDirectory(File f) {
        this.outputDirectory = f;
        log.info("Workflow output base directory: " + this.outputDirectory);
    }

    @Override
    public List<IWorkflowPostProcessor> getWorkflowPostProcessors() {
        return workflowPostProcessors;
    }

    @Override
    public void setWorkflowPostProcessors(List<IWorkflowPostProcessor> workflowPostProcessors) {
        this.workflowPostProcessors = workflowPostProcessors;
    }

    @Override
    public IFactory getFactory() {
        return factory;
    }

    @Override
    public void setFactory(IFactory factory) {
        if (this.factory != null) {
            throw new IllegalStateException("Factory was already initialized!");
        }
        this.factory = factory;
    }

    @Override
    public void clearResults() {
        this.al.clear();
    }
}
