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
package cross.applicationContext;

import cross.annotations.AnnotationInspector;
import cross.commands.fragments.AFragmentCommand;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.Lookup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This code is based on
 * https://github.com/mangstadt/Spring-Application-Context-Generator but uses
 * the compiled class information via reflection and the java service
 * provider/netbeans lookup API in order to resolve references. Values are
 * populated with their default values from the instantiated objects.
 *
 * The Generator will accept any number of concrete or abstract fully qualified
 * class names and will introspect the associated classes. Abstract classes will
 * be used to query for corresponding service implementations. Classes must have
 * a default no-args constructor in order for the service loader facility to
 * work properly, so there are no constructor-arg elements created at the
 * moment.
 *
 * @author Nils Hoffmann
 */
@Slf4j
public class ReflectionApplicationContextGenerator {

    /**
     * Creates a stub application context xml file for the given classes. The
     * default bean scope is 'prototype'.
     *
     * @param outputFile the output file for the generated xml file
     * @param springVersion the spring version to use for validation
     * @param defaultScope the default scope
     * @param classes the classes to generate stubs for
     */
    public static void createContextXml(File outputFile, String springVersion, String defaultScope, String... classes) {
        //generate the application context XML
        ReflectionApplicationContextGenerator generator = new ReflectionApplicationContextGenerator(springVersion, defaultScope);
        for (String className : classes) {
            try {
                Collection<?> serviceImplementations = Lookup.getDefault().lookupAll(Class.forName(className));
                if (serviceImplementations.isEmpty()) {
                    //standard bean, add it
                    try {
                        generator.addBean(className);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {//service provider implementation
                    for (Object obj : serviceImplementations) {
                        try {
                            generator.addBean(obj.getClass().getCanonicalName());
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        Document document = generator.getDocument();
        Element element = document.getDocumentElement();
        Comment comment = document.createComment("In order to use this template in a runnable pipeline, please include it from another application context xml file.");
        element.getParentNode().insertBefore(comment, element);
        writeToFile(document, outputFile);
    }

    /**
     * Write the given xml document to the outputFile.
     *
     * @param document the xml document
     * @param outputFile the output file
     */
    public static void writeToFile(Document document, File outputFile) {
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            transfac.setAttribute("indent-number", 2);
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(document);
            BufferedOutputStream bw = null;
            try {
                outputFile.getParentFile().mkdirs();
                log.info("Writing to file {}", outputFile.getAbsolutePath());
                bw = new BufferedOutputStream(new FileOutputStream(outputFile));
                trans.transform(domSource, new StreamResult(new OutputStreamWriter(bw, "utf-8")));
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            //System.out.println(xmlString);
        } catch (TransformerException ex) {
            Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        String[] classes = args;
        String springVersion = "3.0";
        String scope = "prototype";
        //generate the application context XML
        ReflectionApplicationContextGenerator generator = new ReflectionApplicationContextGenerator(springVersion, scope);
        for (String className : classes) {
            try {
                Collection<?> serviceImplementations = Lookup.getDefault().lookupAll(Class.forName(className));
                if (serviceImplementations.isEmpty()) {
                    //standard bean, add it
                    try {
                        generator.addBean(className);
                    } catch (ClassNotFoundException ex) {
                        log.warn("Could not find class", ex);
                    }
                } else {//service provider implementation
                    for (Object obj : serviceImplementations) {
                        try {
                            generator.addBean(obj.getClass().getCanonicalName());
                        } catch (ClassNotFoundException ex) {
                            log.warn("Could not find class", ex);
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                log.warn("Could not find class", ex);
            }

        }
        Document document = generator.getDocument();

        writeToFile(document, new File("applicationContext.xml"));
    }
    /**
     * The list of Java primitive types.
     */
    private static final List<String> primitives = Arrays.asList(new String[]{"byte", "short", "char", "int", "long", "float", "double", "boolean"});
    /**
     * The list of Java wrapper classes (includes String).
     */
    private static final List<String> wrappers = Arrays.asList(new String[]{"Byte", "Short", "Character", "Integer", "Long", "Float", "Double", "Boolean", "String"});
    /**
     * The XML document.
     */
    private final Document document;
    /**
     * The XML root element.
     */
    private final Element root;
    private LinkedHashMap<String, BeanDescriptor> classToElement = new LinkedHashMap<>();
    private HashMap<Class<?>, List<Object>> classToObject = new HashMap<>();
    private String defaultScope = "prototype";

    /**
     * Constructs a new application context generator.
     *
     * @param springVersion the Spring version
     * @param defaultScope
     */
    public ReflectionApplicationContextGenerator(String springVersion, String defaultScope) {
        //create the XML document
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            //never thrown in my case, so ignore it
        }
        document = docBuilder.newDocument();

        //create the root element
        root = document.createElementNS("http://www.springframework.org/schema/beans", "beans");
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-" + springVersion + ".xsd");
        document.appendChild(root);
        this.defaultScope = defaultScope;
    }

    /**
     * Gets the generated XML document.
     *
     * @return the XML document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Adds a bean to the application context using a Java source file. Only
     * public classes are added.
     *
     * @param className
     * @return this
     * @throws java.lang.ClassNotFoundException
     */
    public ReflectionApplicationContextGenerator addBean(String className) throws ClassNotFoundException {
        Class<?> clazz;
        clazz = Class.forName(className);
//        if (classToObject.containsKey(clazz)) {
//            System.out.println("Class " + clazz.getCanonicalName() + " already known!");
//            return this;
//        }
//        if (!classToObject.containsKey(clazz)) {
        createElement(clazz);
//        }

        return this;
    }

    /**
     * Returns the known service providers for the given service interface name.
     *
     * @param serviceInterfaceName the fully qualified service interface class
     * name
     * @return the list of service providers
     */
    public List<?> getServiceProviders(String serviceInterfaceName) {
        try {
            return createServiceProviderElements(Class.forName(serviceInterfaceName));
        } catch (ClassNotFoundException ex) {
            log.warn("Exception while instantiating services:", ex);
            return Collections.emptyList();
        }
    }

    /**
     * Create a bean element for the given class.
     *
     * @param clazz the target class
     * @return the bean element or a list of beans for each service provider
     * available
     */
    public List<?> createElement(Class<?> clazz) {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return createServiceProviderElements(clazz);
        }
        if (clazz.getConstructors().length == 0) {
            log.warn("Class {} has no public no-argument constructor!", clazz);
            return Collections.emptyList();
        }
        Object obj;
        try {
            obj = clazz.newInstance();
            //build a "bean" element for each class
            buildBeanElement(obj);
            return Arrays.asList(obj);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Collections.emptyList();
    }

    /**
     * Returns one prototype bean for each service provider known for the given
     * serviceInterface.
     *
     * @param serviceInterface the service interface to look for service
     * providers
     * @return the list of protype beans created by calling each service
     * provider
     */
    public List<?> createServiceProviderElements(Class<?> serviceInterface) {
        if (classToObject.containsKey(serviceInterface)) {
            return classToObject.get(serviceInterface);
        }
        LinkedList<Object> spis = new LinkedList<>(Lookup.getDefault().lookupAll(serviceInterface));
        for (Object obj : spis) {
            buildBeanElement(obj);
        }
        classToObject.put(serviceInterface, spis);
        return spis;
    }

    /**
     * Returns a version of <code>s</code>, with the first character in lower
     * case.
     *
     * @param s the string
     * @return s with lower-cased first character
     */
    public static String toLowerCaseName(String s) {
        return s.substring(0, 1).toLowerCase() + s.substring(1);
    }

    /**
     * Creates the <code>BeanDescriptor</code> element.
     *
     * @param obj the object to create a bean descriptor for
     * @return the bean descriptor
     */
    private BeanDescriptor buildBeanElement(Object obj) {
        String id = toLowerCaseName(obj.getClass().getSimpleName());
        if (classToElement.containsKey(id)) {
            System.out.println("BeanDescriptor with id " + id + " already present!");
        } else {
            BeanDescriptor bd = new BeanDescriptor(this, obj, defaultScope, id);
            Element beanElement = createElement(this, document, bd);
            if (beanElement != null) {
                Comment beanCommentElement = null;
                if (obj instanceof AFragmentCommand) {
                    AFragmentCommand afc = (AFragmentCommand) obj;
                    beanCommentElement = document.createComment(afc.getDescription());
                    root.appendChild(beanCommentElement);
                }
                root.appendChild(beanElement);
                System.out.println("Adding class " + obj.getClass().getCanonicalName());
                classToObject.put(obj.getClass(), Arrays.asList(obj));
                classToElement.put(id, bd);
            } else {
                //System.err.println("Warning: Could not find public class in \"" + file + "\".");
            }
        }
        return classToElement.get(id);
    }

    /**
     *
     * @param clazz
     * @param packages
     * @return
     */
    public static Set<BeanDefinition> getImplementationsOf(Class<?> clazz, String... packages) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(clazz));
        Set<BeanDefinition> beanDefinitions = new LinkedHashSet<>();
        for (String pkg : packages) {
            String pkgDeclaration = "";
            if (pkg.contains(".")) {
                pkgDeclaration = pkg.replaceAll("\\.", "/");
            }
            Set<BeanDefinition> components = provider.findCandidateComponents(pkgDeclaration);
            beanDefinitions.addAll(components);
        }
        return beanDefinitions;
    }
    
    /**
     *
     * @param field
     * @return
     */
    public static Class<?> getGenericFieldType(Field field) {
        ParameterizedType fieldType = (ParameterizedType) field.getGenericType();
        return (Class<?>) fieldType.getActualTypeArguments()[0];
    }
    
    /**
     *
     * @param m
     * @return
     */
    public static Class<?> getGenericMethodReturnType(Method m) {
        ParameterizedType methodReturnType = (ParameterizedType) m.getGenericReturnType();
        return (Class<?>) methodReturnType.getActualTypeArguments()[0];
    }

    /**
     * Checks and adds type information for the given method, class and object
     * to the properties list.
     *
     * @param method the method to check for a property
     * @param javaClass the class to check for settable property
     * @param obj the stub / default object of type javaClass
     * @param properties the properties list
     */
    public static void checkMutableProperties(Method method, Class<?> javaClass, Object obj, List<ObjectProperty> properties) {
        if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
            String methodBaseName = method.getName().startsWith("get") ? method.getName().substring(3) : method.getName().substring(2);
            try {
                //check for corresponding setter
                if (javaClass.getMethod("set" + methodBaseName, method.getReturnType()) != null) {
                    String propertyName = toLowerCaseName(methodBaseName);
//                            System.out.print("Handling property " + propertyName);
                    ObjectProperty p = new ObjectProperty();
                    Class<?> returnType = method.getReturnType();
                    if (returnType.isArray()) {
                        p.type = "Array";
                    } else {
                        p.type = returnType.getCanonicalName();
                    }
                    Class<?> genericReturnType = getGenericMethodReturnType(method);
                    p.genericType = genericReturnType.getCanonicalName();
                    p.name = propertyName;

                    Object methodObject = null;
                    try {
                        methodObject = method.invoke(obj);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        log.warn(ex.getLocalizedMessage());
                    }
                    String value = null;
                    if (methodObject == null) {
                        value = "";
                    } else {
                        if (methodObject.getClass().isArray()) {
                            value = methodObject.getClass().getComponentType().getCanonicalName();
                        } else {
                            value = methodObject.toString();
                        }
                    }
                    if (value == null) {
                        value = "";
                    } else {
                        value = value.trim();
                    }
                    if (value.contains("\"")) {
                        //remove the quotes that surround Strings
                        value = value.replaceAll("\"", "");
                    } else if (value.contains("'")) {
                        //remove the quotes that surround characters
                        value = value.replaceAll("'", "");
                    } else if (value.endsWith("d") || value.endsWith("D") || value.endsWith("f") || value.endsWith("F") || value.endsWith("l") || value.endsWith("L")) {
                        //remove the "double", "float", or "long" letters if they are there
                        value = value.substring(0, value.length() - 1);
                    }
                    p.value = value;

                    System.out.println(" value: " + p.value + " type: " + p.type);
                    properties.add(p);
                }
            } catch (NoSuchMethodException ex) {
                log.info("Ignoring read-only property {}", methodBaseName);
                //Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        }
    }

    /**
     * Creates an xml element using the given document for element creation.
     *
     * @param generator
     * @param document the document use for element creation
     * @param beanDescriptor
     * @return the modified element
     */
    public static Element createElement(ReflectionApplicationContextGenerator generator, Document document, BeanDescriptor beanDescriptor) {
        Class<?> javaClass = beanDescriptor.clazz;
        System.out.println("Building bean element for " + javaClass.getName());
        //get the name of the class
        String className = javaClass.getSimpleName();

        //get the name of the package
        String packageName = javaClass.getPackage().getName();

        //create <bean /> element
        Element beanElement = document.createElement("bean");
        String classNameLower = toLowerCaseName(className);
        beanElement.setAttribute("id", classNameLower);
        String classAttr = (packageName == null) ? className : packageName + "." + className;
        beanElement.setAttribute("class", classAttr);
        beanElement.setAttribute("scope", beanDescriptor.scope);
        //constructors are not supported
        //get all the class' properties from the public fields and setter methods.

        for (Method method : javaClass.getMethods()) {
            checkMutableProperties(method, javaClass, beanDescriptor.obj, beanDescriptor.properties);
        }
        //sort by name
        Collections.sort(beanDescriptor.properties, new Comparator<ObjectProperty>() {
            @Override
            public int compare(ObjectProperty t, ObjectProperty t1) {
                return t.name.compareTo(t1.name);
            }
        });
        List<String> blackList = Arrays.asList("workflow", "progress", "cvResolver");
        //add all properties as <property /> elements
        for (ObjectProperty p : beanDescriptor.properties) {
            if (!blackList.contains(p.name)) {
                Element propertyElement = document.createElement("property");
                propertyElement.setAttribute("name", p.name);
                Comment propertyCommentElement = document.createComment(AnnotationInspector.getDescriptionFor(javaClass, p.name));
                boolean append = true;
                if (p.type.startsWith("java.lang.")) {
                    String shortType = p.type.substring("java.lang.".length());
                    if (primitives.contains(shortType) || wrappers.contains(shortType)) {
                        propertyElement.setAttribute("value", p.value);
                    }
                } else if (primitives.contains(p.type) || wrappers.contains(p.type)) {
                    propertyElement.setAttribute("value", p.value);
                } else if ("Array".equals(p.type)
                        || "List".equals(p.type) || "java.util.List".equals(p.type)) {
                    Element listElement = document.createElement("list");
                    String genericType = p.genericType;
                    propertyElement.appendChild(listElement);
                } else if ("Set".equals(p.type) || "java.util.Set".equals(p.type)) {
                    Element listElement = document.createElement("set");
                    propertyElement.appendChild(listElement);
                } else if ("Map".equals(p.type) || "java.util.Map".equals(p.type)) {
                    Element listElement = document.createElement("map");
                    propertyElement.appendChild(listElement);
                } else if ("Properties".equals(p.type) || "java.util.Properties".equals(p.type)) {
                    Element listElement = document.createElement("props");
                    propertyElement.appendChild(listElement);
                } else {
                    try {
                        //                    System.err.println("Skipping ref!");
                        Set<BeanDefinition> beanDefinitions = getImplementationsOf(Class.forName(p.type), "cross", "maltcms", "net.sf.maltcms");
                        BeanDefinition first = null;
                        for (BeanDefinition bd : beanDefinitions) {
                            generator.addBean(bd.getBeanClassName());
                            if (first == null) {
                                first = bd;
                            }
                        }
                        if (first != null) {
                            String simpleName = first.getBeanClassName().substring(first.getBeanClassName().lastIndexOf(".") + 1);
                            propertyElement.setAttribute("ref", generator.classToElement.get(toLowerCaseName(simpleName)).id);
                        }
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    append = true;
//                    try {
//                        generator.addBean(p.type);
//                        Class<?> c = Class.forName(p.type);
//                        List<Object> objects = generator.classToObject.get(c);
//                        if (objects != null && !objects.isEmpty()) {
//                            propertyElement.setAttribute("ref", generator.buildBeanElement(objects.get(0)).id);
//                        } else {
//                            append = false;
//                        }
//                    } catch (ClassNotFoundException ex) {
//                        Logger.getLogger(ReflectionApplicationContextGenerator.class.getName()).log(Level.SEVERE, null, ex);
//                    }

                }
                if (append) {
                    beanElement.appendChild(propertyCommentElement);
                    beanElement.appendChild(propertyElement);
                } else {
                    beanElement.appendChild(propertyCommentElement);
                    Comment comment = document.createComment("<property name=\"" + p.name + "\" ref=\"\"/>");
                    beanElement.appendChild(comment);
                }
            }
        }
        return beanElement;
    }

    /**
     * Simple holder class for Object related properties.
     */
    public static class ObjectProperty {

        /**
         * The name of the object property
         */
        public String name;
        /**
         * The class type of the object property
         */
        public String type;
        /**
         * The value of the object property
         */
        public String value;
        
        /**
         * The generic class type of the object property
         */
        public String genericType;
    }

    /**
     * Holder class for bean information.
     */
    public static class BeanDescriptor {

        private final ReflectionApplicationContextGenerator generator;
        private final String scope;
        private final Class<?> clazz;
        private final Object obj;
        /**
         *
         */
        public String id;
        /**
         *
         */
        public List<ObjectProperty> properties = new ArrayList<>();

        /**
         * Creates a new bean descriptor from the given information.
         *
         * @param generator
         * @param obj
         * @param scope
         * @param id
         */
        public BeanDescriptor(ReflectionApplicationContextGenerator generator, Object obj, String scope, String id) {
            this.generator = generator;
            this.clazz = obj.getClass();
            this.obj = obj;
            this.scope = scope;
            this.id = id;
        }

    }
}
