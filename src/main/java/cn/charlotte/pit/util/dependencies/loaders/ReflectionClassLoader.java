/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package cn.charlotte.pit.util.dependencies.loaders;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ReflectionClassLoader implements PluginClassLoader {
    private static final Method ADD_URL_METHOD;
    private static final boolean ACCESS_AVAILABLE;

    static {
        Method method = null;
        boolean accessAvailable = false;
        
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            try {
                method.setAccessible(true);
                accessAvailable = true;
            } catch (java.lang.reflect.InaccessibleObjectException e) {
                // Java 17+ module system prevents access
                System.err.println("Warning: Cannot access URLClassLoader.addURL method due to Java module system restrictions.");
                System.err.println("To run on Java 21+, start the server with: --add-opens=java.base/java.net=ALL-UNNAMED");
            }
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof java.lang.reflect.InaccessibleObjectException) {
                System.err.println("Warning: Cannot access URLClassLoader.addURL method due to Java module system restrictions.");
                System.err.println("To run on Java 21+, start the server with: --add-opens=java.base/java.net=ALL-UNNAMED");
            } else {
                throw new ExceptionInInitializerError(e);
            }
        }
        
        ADD_URL_METHOD = method;
        ACCESS_AVAILABLE = accessAvailable;
    }

    private final URLClassLoader classLoader;

    public ReflectionClassLoader(Object plugin) throws IllegalStateException {
        ClassLoader classLoader = plugin.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = (URLClassLoader) classLoader;
        } else {
            throw new IllegalStateException("ClassLoader is not instance of URLClassLoader");
        }
    }

    @Override
    public void loadJar(Path file) {
        if (!ACCESS_AVAILABLE) {
            throw new RuntimeException(new IllegalAccessException(
                "Cannot access URLClassLoader.addURL method due to Java module system restrictions. " +
                "To run on Java 21+, start the server with: --add-opens=java.base/java.net=ALL-UNNAMED"
            ));
        }
        
        try {
            ADD_URL_METHOD.invoke(this.classLoader, file.toUri().toURL());
        } catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
