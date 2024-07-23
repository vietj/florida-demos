package io.vertx.classloading;

import io.vertx.core.impl.IsolatingClassLoader;

import java.net.URL;
import java.util.List;

public class IsolatingClassLoaderFactory {
  public static ClassLoader isolatingClassLoader(URL[] urls, ClassLoader parent, List<String> isolatedClasses) {
    return new IsolatingClassLoader(urls, parent, isolatedClasses);
  }
}
