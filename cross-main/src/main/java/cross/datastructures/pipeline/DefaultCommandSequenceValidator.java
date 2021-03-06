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
package cross.datastructures.pipeline;

import cross.annotations.AnnotationInspector;
import cross.commands.fragments.IFragmentCommand;
import cross.datastructures.fragments.IFileFragment;
import cross.datastructures.fragments.IVariableFragment;
import cross.datastructures.tuple.TupleND;
import cross.exception.ConstraintViolationException;
import cross.exception.ResourceNotAvailableException;
import cross.vocabulary.CvResolver;
import cross.vocabulary.ICvResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation for command sequence validation.
 *
 * Set {@link #checkInheritedVariables} to false if you do NOT want to check
 * variables from previous, referenced pipeline result FileFragments. This will
 * however defeat the purpose of pipeline validation.
 *
 * @author Nils Hoffmann
 */
@Data
@Slf4j
public class DefaultCommandSequenceValidator implements ICommandSequenceValidator {

    /**
     * Determines, whether inherited variables from ancestor file fragments
     * should be checked, when immediate variables are not available. 
     * -- SETTER-- 
     * Set whether inherited variables should be checked.
     * 
     * @param true if inherited variables should be checked, false otherwise
     * @return whether inherited variables should be checked
     */
    private boolean checkInheritedVariables = true;
    /**
     * The {@link CvResolver} to use for controlled vocabulary term resolution.
     * -- SETTER -- 
     * Set the cv resolver.
     * 
     * @param the cv resolver to set
     * @return the current cv resolver
     */
    private ICvResolver resolver = new CvResolver();

    @Override
    public boolean isValid(ICommandSequence commandSequence) throws ConstraintViolationException {
        try {
            checkCommandDependencies(commandSequence.getInput(), commandSequence.getCommands());
            return true;
        } catch (ConstraintViolationException cve) {
            log.warn("Pipeline validation failed: " + cve.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Check the command dependencies of the given input fragments and commands.
     *
     * @param inputFragments the input fragments to check
     * @param commands the commands to check
     */
    public void checkCommandDependencies(
            TupleND<IFileFragment> inputFragments,
            List<IFragmentCommand> commands) {
        final HashSet<String> providedVariables = new HashSet<>();
        for (IFragmentCommand cmd : commands) {
            if (this.checkInheritedVariables) {
                // required variables
                final Collection<String> requiredVars = AnnotationInspector.getRequiredVariables(cmd);
                // optional variables
                final Collection<String> optionalVars = AnnotationInspector.getOptionalRequiredVariables(cmd);
                // get variables provided from the past
                getPersistentVariables(inputFragments, requiredVars,
                        providedVariables);
                getPersistentVariables(inputFragments, optionalVars,
                        providedVariables);
                // check dependencies
                // The following method throws a RuntimeException, when its
                // constraints are not met, e.g. requiredVariables are not
                // present, leading to a termination
                checkRequiredVariables(cmd, requiredVars, providedVariables);
                checkOptionalVariables(cmd, optionalVars, providedVariables);
            }

            // provided variables
            final Collection<String> createdVars = AnnotationInspector.getProvidedVariables(cmd);
            for (final String var : createdVars) {
                if (!var.isEmpty() && !providedVariables.contains(var)) {
                    log.debug("Adding new variable {}, provided by {}",
                            var, cmd.getClass().getName());
                    providedVariables.add(var);
                } else {
                    log.debug(
                            "Variable {} is shadowed!",
                            var);
                }
            }
        }
    }

    /**
     * Check required optional variables against provided variables for given
     * cmd.
     *
     * @param cmd the command to check
     * @param optionalVars the optional variables
     * @param providedVariables the provided variables
     * @return the collection of optional variables
     */
    protected Collection<String> checkOptionalVariables(
            final IFragmentCommand cmd, final Collection<String> optionalVars,
            final HashSet<String> providedVariables) {
        if (optionalVars.isEmpty()) {
            log.debug("No optional variables declared!");
            return optionalVars;
        }
        boolean checkOpt = true;
        for (final String var : optionalVars) {
            if (!var.isEmpty() && !providedVariables.contains(var)) {
                log.debug(
                        "Variable {} requested as optional by {} not declared as created by previous commands!",
                        var, cmd.getClass().getName());
                checkOpt = false;
            }

        }
        if (checkOpt && (optionalVars.size() > 0)) {
            log.debug(
                    "Command {} has access to all optional requested variables!",
                    cmd.getClass().getName());
        }
        return optionalVars;
    }

    /**
     * Check required variables against provided variables against given cmd.
     *
     * @param cmd the command to check
     * @param requiredVars the required variables
     * @param providedVariables the provided variables
     * @return the collection of required variables
     * @throws ConstraintViolationException
     */
    protected Collection<String> checkRequiredVariables(
            final IFragmentCommand cmd, final Collection<String> requiredVars,
            final HashSet<String> providedVariables)
            throws ConstraintViolationException {
        if (requiredVars.isEmpty()) {
            log.debug("No required variables declared!");
            return requiredVars;
        }
        boolean check = true;
        final Collection<String> failedVars = new ArrayList<>();
        for (final String var : requiredVars) {
            log.debug("Checking variable {}", var);
            if (!var.isEmpty() && !providedVariables.contains(var)) {
                log.warn(
                        "Variable {} requested by {} not declared as created by previous commands!",
                        var, cmd.getClass().getName());
                check = false;
                failedVars.add(var);
            }
        }
        if (check) {
            if (requiredVars.size() > 0) {
                log.debug(
                        "Command {} has access to all required variables!", cmd.getClass().getName());
            }
            return requiredVars;
        } else {
            throw new ConstraintViolationException("Command "
                    + cmd.getClass().getName()
                    + " requires non-existing variables: "
                    + failedVars.toString());
        }
    }

    /**
     * Retrieve persistent variables, stored in previous pipeline result file
     * fragments.
     *
     * @param inputFragments the input fragments to check
     * @param requiredVariables the required variables to check the fragments
     * for
     * @param providedVariables the provided variables to add the available
     * variables to
     */
    private void getPersistentVariables(
            final TupleND<IFileFragment> inputFragments,
            final Collection<String> requiredVariables,
            final HashSet<String> providedVariables) {
        for (final IFileFragment ff : inputFragments) {
            for (final String s : requiredVariables) {
                // resolve the variables name
                if ((s != null) && !s.isEmpty()) {
                    final String vname = resolver.translate(s);
                    try {
                        final IVariableFragment ivf = ff.getChild(vname, true);
                        log.debug("Retrieved var {}", ivf.getName());
                        if (!providedVariables.contains(s)) {
                            providedVariables.add(s);
                        }
                    } catch (final ResourceNotAvailableException rnae) {
                        log.debug(
                                "Could not find variable {} as child of {}",
                                vname, ff.getUri());
                    }
                } else {
                    throw new ConstraintViolationException("Variable name of required variable must not be null!");
                }
            }
        }
    }
}
