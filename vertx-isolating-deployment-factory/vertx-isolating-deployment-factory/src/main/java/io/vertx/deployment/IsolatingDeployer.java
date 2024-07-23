package io.vertx.deployment;

import io.vertx.classloading.IsolatingClassLoaderFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Deployment;
import io.vertx.core.impl.VertxInternal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IsolatingDeployer {

  private final Vertx vertx;
  private final Map<String, Loader> classLoaderMap = new HashMap<>();

  private static class Loader {
    private final ClassLoader classLoader;
    private int refCount;
    public Loader(ClassLoader classLoader, int refCount) {
      this.classLoader = classLoader;
      this.refCount = refCount;
    }
  }

  public IsolatingDeployer(Vertx vertx) {
    this.vertx = vertx;
  }

  public Future<String> deploy(String name,
                               DeploymentOptions options,
                               String isolationGroup,
                               List<String> extraClasspath,
                               List<String> isolatedClasses) {

    ClassLoader classLoader;
    synchronized (classLoaderMap) {
      Loader loader = classLoaderMap.get(isolationGroup);
      if (loader == null) {
        URL[] urls = extraClasspath.stream().map(IsolatingDeployer::mapToURL).toArray(URL[]::new);
        classLoader = IsolatingClassLoaderFactory.isolatingClassLoader(urls, Thread.currentThread().getContextClassLoader(), isolatedClasses);
        loader = new Loader(classLoader, 1);
        classLoaderMap.put(isolationGroup, loader);
      } else {
        classLoader = loader.classLoader;
      }
    }

    options = new DeploymentOptions(options).setClassLoader(classLoader);

    return vertx.deployVerticle(name, options).onComplete(ar -> {
      if (ar.succeeded()) {
        String deploymentId = ar.result();
        Deployment deployment = ((VertxInternal) vertx).getDeployment(deploymentId);
        if (deployment != null) {
          deployment.undeployHandler(v -> {
            close(isolationGroup);
          });
          return;
        }
      }
      // Cleanup
      close(isolationGroup);
    });
  }

  private void close(String isolationGroup) {
    synchronized (classLoaderMap) {
      Loader loader = classLoaderMap.get(isolationGroup);
      if (loader != null) {
        if (--loader.refCount == 0) {
          classLoaderMap.remove(isolationGroup);
        }
      } else {
        // bug ???
      }
    }
  }

  private static URL mapToURL(String path) {
    try {
      return new URL(path);
    } catch (MalformedURLException e) {
      try {
        return new File(path).toURI().toURL();
      } catch (MalformedURLException e1) {
        throw new IllegalArgumentException(e1);
      }
    }
  }
}
