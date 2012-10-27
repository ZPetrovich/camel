/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.gson;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class GsonMarshalTest extends CamelTestSupport {

    @Test
    public void testMarshalAndUnmarshalMap() throws Exception {
        Map<String, String> in = new HashMap<String, String>();
        in.put("name", "Camel");

        MockEndpoint mock = getMockEndpoint("mock:reverse");
        mock.expectedMessageCount(1);
        mock.message(0).body().isInstanceOf(Map.class);
        mock.message(0).body().equals(in);

        Object marshalled = template.requestBody("direct:in", in);
        String marshalledAsString = context.getTypeConverter().convertTo(String.class, marshalled);
        assertEquals("{\"name\":\"Camel\"}", marshalledAsString);

        template.sendBody("direct:back", marshalled);

        mock.assertIsSatisfied();
    }
    
   
    @Test
    @SuppressWarnings("rawtypes")
    public void testUnmarshalMap() throws Exception {
        Map unmarshalled = 
            template.requestBody("direct:json", "{\"pointsOfSale\":{\"pointOfSale\":{\"prodcut\":\"newpad\"}}}", Map.class);
        Map map1 = (Map)unmarshalled.get("pointsOfSale");
        Map map2 = (Map) map1.get("pointOfSale");
        assertEquals("Don't get the right value", "newpad", map2.get("prodcut"));
    }

    @Test
    public void testMarshalAndUnmarshalPojo() throws Exception {
        TestPojo in = new TestPojo();
        in.setName("Camel");

        MockEndpoint mock = getMockEndpoint("mock:reversePojo");
        mock.expectedMessageCount(1);
        mock.message(0).body().isInstanceOf(TestPojo.class);
        mock.message(0).body().equals(in);

        Object marshalled = template.requestBody("direct:inPojo", in);
        String marshalledAsString = context.getTypeConverter().convertTo(String.class, marshalled);
        assertEquals("{\"name\":\"Camel\"}", marshalledAsString);

        template.sendBody("direct:backPojo", marshalled);

        mock.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                GsonDataFormat format = new GsonDataFormat();

                from("direct:in").marshal(format);
                from("direct:back").unmarshal(format).to("mock:reverse");

                GsonDataFormat formatPojo = new GsonDataFormat(TestPojo.class);

                from("direct:inPojo").marshal(formatPojo);
                from("direct:backPojo").unmarshal(formatPojo).to("mock:reversePojo");
                
                from("direct:json").unmarshal().json(JsonLibrary.Gson, Map.class);
            }
        };
    }

}