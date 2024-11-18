/*
 * qStudio - Free SQL Analysis Tool
 * Copyright C 2013-2024 TimeStored
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.timestored.misc;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.collect.Lists;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
public class DynamicClassLoader {

	private DynamicClassLoader() {}
	

	/**
	 * Searches a dir for jar files and creates an instance of any that implement a certain interface.
	 * @param onlyLoadFirstMatch Only load the first match, don't continue to search for more instances.
	 * @return Instances of classes within a directory/jar that implemented the given interface.
	 */
	public static <T> List<T> loadInstances(File dir, Class<T> interfaceWanted, boolean onlyLoadFirstMatch) {
        log.info("Searching for plugins in folder: " + dir.getAbsolutePath());
		InstanceFinderVisitor<T> v = new InstanceFinderVisitor<T>(interfaceWanted, onlyLoadFirstMatch);
		try {
			visitJarFileJavaClasses(dir, v);
		} catch (IOException e) {
			log.warning("Problem while searching for plugins" + e.toString());
		}
		return v.instances;
	}
	


	
    /**
     * Finds the first class that is a certain "instanceof" and stops.
     * Can then access that found object. 
     */
	@RequiredArgsConstructor
    private static class InstanceFinderVisitor<T> implements Visitor<ClassDetails> {

    	private final Class<T> interfaceWanted;
    	private final boolean stopAtFirstMatch;
    	@Getter private final List<T> instances = Lists.newArrayList();

		@Override public boolean visit(ClassDetails classDetails) {
            try {
				URLClassLoader loader = new URLClassLoader(new URL[]{classDetails.classUrl});
				try {
				    Class c = loader.loadClass(classDetails.className);
				    Class[] interfaces = c.getInterfaces();
				    for (int i = 0;i < interfaces.length;i++) {
				        if (interfaceWanted == interfaces[i]) {
				        	log.info("Found Plugin with correct interface: " + classDetails.className);
				            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL",new Class[]{URL.class});
				            addURL.setAccessible(true);
				            ClassLoader cl = ClassLoader.getSystemClassLoader();
				            addURL.invoke(cl,new Object[]{classDetails.classUrl});
				            Object o = c.newInstance();
				            this.instances.add((T) o);
				            return !stopAtFirstMatch;
				        }
					}
				} catch(NoClassDefFoundError e) {
				} catch(IllegalAccessError e) {
				} catch(VerifyError e) {
				}
            } catch(IllegalAccessException e) {
            } catch (NoSuchMethodException e) {
            } catch (SecurityException e) {
			} catch (IllegalArgumentException e) {
			} catch (InvocationTargetException e) {
			} catch (InstantiationException e) {
			} catch (ClassNotFoundException e) {
			}
            
			return true;
		}
    	
    }

    /**
     * Visit all the java classes from jar's etc in the dir folder.
     */
    private static void visitJarFileJavaClasses(File dir, Visitor<ClassDetails> visitor) throws IOException  {

        if (!dir.exists())
            return;

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir,String name) {
                return name.endsWith(".jar");
            }
        };

        String[] children = dir.list(filter);
        if (children != null) {
            for (int child = 0;child < children.length;child++) {
                String filename = dir.getAbsolutePath() + "/" + children[child];
                log.info("Looking for plugins inside: " + filename);
                
                URL url = new URL("jar:file:" + filename + "/!/");
                JarURLConnection conn = (JarURLConnection) url.openConnection();
                JarFile jarFile = conn.getJarFile();

                Enumeration<JarEntry> e = jarFile.entries();
                while (e.hasMoreElements()) {
                    JarEntry entry = e.nextElement();
                    String name = entry.getName();
                    if (!entry.isDirectory() && name.endsWith(".class")) {
                        String externalName = name.substring(0,name.indexOf('.')).replace('/','.');
                        if(!visitor.visit(new ClassDetails(externalName, url))) {
                        	return;
                        }
                    }
                }
            }
        }
        
        return;
    }

    @Data private static class ClassDetails {
    	private final String className;
    	private final URL classUrl;
    }

}
