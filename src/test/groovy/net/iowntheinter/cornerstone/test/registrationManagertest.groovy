package net.iowntheinter.cornerstone.test

import io.vertx.ext.unit.TestSuite

/**
 * Created by g on 10/15/16.
 */
TestSuite suite = TestSuite.create("registrationManager")
suite.test("startup", {context ->
  
  String s = "value"
  context.assertEquals("value", s)
})
suite.run()