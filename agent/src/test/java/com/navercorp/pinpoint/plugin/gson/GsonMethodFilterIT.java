/**
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package com.navercorp.pinpoint.plugin.gson;

import static com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier.ExpectedAnnotation.*;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * @author ChaYoung You
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({"com.google.code.gson:gson:(,1.7),(1.7,)"})
public class GsonMethodFilterIT {
    private static final boolean v1_2;
    private static final boolean v1_6;
    
    static {
        Method m = null;
        try {
            m = Gson.class.getMethod("fromJson", Reader.class, Class.class);
        } catch (NoSuchMethodException e) {
        }
        
        v1_2 = m != null;
        
        Class<?> c = null;
        try {
            c = Class.forName("com.google.gson.stream.JsonReader");
        } catch (ClassNotFoundException e) {
        }
        
        v1_6 = c != null;
    }
    
    private static final String java = "Pinpoint";
    private static final String json = new Gson().toJson(java);
    private static final String serviceType = "GSON";
    private static final String annotationKeyName = "gson.json.length";
    private static final JsonElement jsonElement = new JsonParser().parse(json);

    // Pinpoint
    private static final PluginTestVerifier.ExpectedAnnotation fromJsonAnnotation = annotation(annotationKeyName, json.length());
    // "Pinpoint"
    private static final PluginTestVerifier.ExpectedAnnotation toJsonAnnotation = annotation(annotationKeyName, json.length());

    @Test
    public void test() throws Exception {
        final Gson gson = new Gson();
        
        /**
         * @see Gson#fromJson(String, Class)
         * @see Gson#fromJson(String, Type)
         */
        gson.fromJson(json, String.class);
        gson.fromJson(json, (Type) String.class);

        Method fromJson1 = Gson.class.getDeclaredMethod("fromJson", String.class, Class.class);
        Method fromJson2 = Gson.class.getDeclaredMethod("fromJson", String.class, Type.class);

        /**
         * @see Gson#toJson(Object)
         * @see Gson#toJson(Object, Type)
         */
        gson.toJson(java);
        gson.toJson(java, String.class);

        Method toJson1 = Gson.class.getDeclaredMethod("toJson", Object.class);
        Method toJson2 = Gson.class.getDeclaredMethod("toJson", Object.class, Type.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);

        verifyTraceBlockAnnotation(verifier, serviceType, fromJson1, fromJsonAnnotation);
        verifyTraceBlockAnnotation(verifier, serviceType, fromJson2, fromJsonAnnotation);

        verifyTraceBlockAnnotation(verifier, serviceType, toJson1, toJsonAnnotation);
        verifyTraceBlockAnnotation(verifier, serviceType, toJson2, toJsonAnnotation);

        // No more traces
        verifier.verifyTraceBlockCount(0);
    }
    
    @Test
    public void testFromV1_2() throws Exception {
        if (!v1_2) {
            return;
        }
        
        final Gson gson = new Gson();
        
        /**
         * @see Gson#fromJson(Reader, Class)
         * @see Gson#fromJson(Reader, Type)
         * @see Gson#fromJson(JsonElement, Class)
         * @see Gson#fromJson(JsonElement, Type)
         */
        gson.fromJson(new StringReader(json), (Class) String.class);
        gson.fromJson(new StringReader(json), (Type) String.class);
        gson.fromJson(jsonElement, String.class);
        gson.fromJson(jsonElement, (Type) String.class);

        Method fromJson3 = Gson.class.getDeclaredMethod("fromJson", Reader.class, Class.class);
        Method fromJson4 = Gson.class.getDeclaredMethod("fromJson", Reader.class, Type.class);
        Method fromJson6 = Gson.class.getDeclaredMethod("fromJson", JsonElement.class, Class.class);
        Method fromJson7 = Gson.class.getDeclaredMethod("fromJson", JsonElement.class, Type.class);

        /**
         * @see Gson#toJson(Object, Appendable)
         * @see Gson#toJson(Object, Type, Appendable)
         * @see Gson#toJson(JsonElement)
         * @see Gson#toJson(JsonElement, Appendable)
         */
        gson.toJson(java, new StringWriter());
        gson.toJson(java, String.class, new StringWriter());
        gson.toJson(jsonElement);
        gson.toJson(jsonElement, new StringWriter());

        Method toJson3 = Gson.class.getDeclaredMethod("toJson", Object.class, Appendable.class);
        Method toJson4 = Gson.class.getDeclaredMethod("toJson", Object.class, Type.class, Appendable.class);
        Method toJson6 = Gson.class.getDeclaredMethod("toJson", JsonElement.class);
        Method toJson7 = Gson.class.getDeclaredMethod("toJson", JsonElement.class, Appendable.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);

        verifier.verifyApi(serviceType, fromJson3);
        verifier.verifyApi(serviceType, fromJson4);
        verifier.verifyApi(serviceType, fromJson6);
        verifier.verifyApi(serviceType, fromJson7);

        verifier.verifyApi(serviceType, toJson3);
        verifier.verifyApi(serviceType, toJson4);
        verifyTraceBlockAnnotation(verifier, serviceType, toJson6, toJsonAnnotation);
        verifier.verifyApi(serviceType, toJson7);

        // No more traces
        verifier.verifyTraceBlockCount(0);
    }

    
    @Test
    public void testFromV1_6() throws Exception {
        if (!v1_6) {
            return;
        }
        
        final Gson gson = new Gson();
        
        /**
         * @see Gson#fromJson(JsonReader, Type)
         */
        gson.fromJson(new JsonReader(new StringReader(json)), String.class);
        
        Method fromJson5 = Gson.class.getDeclaredMethod("fromJson", JsonReader.class, Type.class);
        
        /**
         * @see Gson#toJson(Object, Type, JsonWriter)
         * @see Gson#toJson(JsonElement, JsonWriter)
         */
        gson.toJson(java, String.class, new JsonWriter(new StringWriter()));
        gson.toJson(jsonElement, new JsonWriter(new StringWriter()));

        Method toJson5 = Gson.class.getDeclaredMethod("toJson", Object.class, Type.class, JsonWriter.class);
        Method toJson8 = Gson.class.getDeclaredMethod("toJson", JsonElement.class, JsonWriter.class);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache(System.out);
        verifier.printBlocks(System.out);

        verifier.verifyApi(serviceType, fromJson5);

        verifier.verifyApi(serviceType, toJson5);
        verifier.verifyApi(serviceType, toJson8);

        // No more traces
        verifier.verifyTraceBlockCount(0);
    }

    private void verifyTraceBlockAnnotation(PluginTestVerifier verifier, String serviceType, Member api, PluginTestVerifier.ExpectedAnnotation annotation) {
        verifier.verifyTraceBlock(PluginTestVerifier.BlockType.EVENT, serviceType, api, null, null, null, null, annotation);
    }
}
