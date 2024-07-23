package io.vertx.uring;

import com.example.TestVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.deployment.IsolatingDeployer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.sql.Time;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SimpleTest {

  private static final URL TEST_CLASSES = TestVerticle.class.getProtectionDomain().getCodeSource().getLocation();

  private Vertx vertx;
  private IsolatingDeployer deployer;
  private List<String> extraClasspath;

  @Before
  public void setup() throws Exception {
    extraClasspath = Collections.singletonList(new File(TEST_CLASSES.toURI()).getAbsolutePath());
    vertx = Vertx.vertx();
    deployer = new IsolatingDeployer(vertx);
  }

  @After
  public void after() throws Exception {
    vertx.close().toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
  }

  @Test
  public void testSameIsolationGroup() throws Exception {
    Map<String, Integer> results = testIsolationGroup(i -> "somegroup");
    Map<String, Integer> expected = new HashMap<>();
    expected.put("verticle-0", 0);
    expected.put("verticle-1", 1);
    expected.put("verticle-2", 2);
    assertEquals(expected, results);
  }

  @Test
  public void testDifferentIsolationGroup() throws Exception {
    Map<String, Integer> results = testIsolationGroup(i -> "somegroup-" + i);
    Map<String, Integer> expected = new HashMap<>();
    expected.put("verticle-0", 0);
    expected.put("verticle-1", 0);
    expected.put("verticle-2", 0);
    assertEquals(expected, results);
  }

  private Map<String, Integer> testIsolationGroup(Function<Integer, String> isolationGroup) throws Exception {
    Map<String, Integer> results = new HashMap<>();
    for (int i = 0;i < 3;i++) {
      String address = "verticle-" + i;
      vertx.eventBus().<Integer>consumer(address, msg -> {
        results.put(address, msg.body());
        msg.reply("OK");
      });
      JsonObject config = new JsonObject().put(TestVerticle.TEST_ADDRESS, address);
      Future<String> fut = deployer.deploy("java:" + TestVerticle.class.getName(),
        new DeploymentOptions().setConfig(config),
        isolationGroup.apply(i),
        extraClasspath,
        Collections.singletonList(TestVerticle.class.getName()));
      fut.toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }
    return results;
  }

  @Test
  public void testCloseGroup() throws Exception {
    String addr1 = "verticle-0";
    AtomicReference<Integer> res1 = new AtomicReference<>();
    vertx.eventBus().<Integer>consumer(addr1, msg -> {
      res1.set(msg.body());
      msg.reply("");
    });
    JsonObject config1 = new JsonObject().put(TestVerticle.TEST_ADDRESS, addr1);
    Future<String> fut1 = deployer.deploy(
      "java:" + TestVerticle.class.getName(),
      new DeploymentOptions().setConfig(config1),
      "group",
      extraClasspath,
      Collections.singletonList(TestVerticle.class.getName()));
    String id1 = fut1.toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals(0, (int)res1.get());
    String addr2 = "verticle-1";
    AtomicReference<Integer> res2 = new AtomicReference<>();
    vertx.eventBus().<Integer>consumer(addr2, msg -> {
      res2.set(msg.body());
      msg.reply("");
    });
    JsonObject config2 = new JsonObject().put(TestVerticle.TEST_ADDRESS, addr2);
    Future<String> fut2 = deployer.deploy(
      "java:" + TestVerticle.class.getName(),
      new DeploymentOptions().setConfig(config2),
      "group",
      extraClasspath,
      Collections.singletonList(TestVerticle.class.getName()));
    String id2 = fut2.toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals(1, (int)res2.get());
    vertx.undeploy(id1).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    vertx.undeploy(id2).toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    String addr3 = "verticle-3";
    AtomicReference<Integer> res3 = new AtomicReference<>();
    vertx.eventBus().<Integer>consumer(addr3, msg -> {
      res3.set(msg.body());
      msg.reply("");
    });
    JsonObject config3 = new JsonObject().put(TestVerticle.TEST_ADDRESS, addr3);
    Future<String> fut3 = deployer.deploy(
      "java:" + TestVerticle.class.getName(),
      new DeploymentOptions().setConfig(config3),
      "group",
      extraClasspath,
      Collections.singletonList(TestVerticle.class.getName()));
    String id3 = fut3.toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals(0, (int)res3.get());
  }
}
