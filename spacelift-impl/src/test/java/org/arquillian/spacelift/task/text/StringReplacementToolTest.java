/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.spacelift.task.text;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.task.io.FileReader;
import org.arquillian.spacelift.task.io.FileSelector;
import org.arquillian.spacelift.task.io.WriteToFileTool;
import org.arquillian.spacelift.task.text.StringReplacementTool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class StringReplacementToolTest {

    private File tempFile = null;

    private String contentBefore = "this is some testing (temporary) file where I want to\n" +
            "replace strings for another ones by Spacelift Sed tool\n" +
            "This is the third row in it.";

    private String contentAfter1 = "this is sOme testing (tempOrary) file where I want tO\n" +
            "replace strings fOr anOther Ones by Spacelift Sed tOOl\n" +
            "This is the third rOw in it.";

    private String contentAfter2 = ".... .. .... ....... ........... .... ..... . .... ..\n" +
            "....... ....... ... ....... .... .. ......... ... ....\n" +
            ".... .. ... ..... ... .. ...";

    private String contentAfter3 = "thisisis isisis some testing (temporary) file where I want to\n" +
            "replace strings for another ones by Spacelift Sed tool\n" +
            "Thisisis isisis the third row in it.";

    @Before
    public void before() throws IOException {
        tempFile = File.createTempFile("sed-test", ".tmp");

        Spacelift.task(WriteToFileTool.class)
                .write(contentBefore).to(tempFile)
                .execute().await();
    }

    @After
    public void after() {
        if (!tempFile.delete()) {
            throw new IllegalStateException("Unable to delete file: " + tempFile.getAbsoluteFile());
        }
    }

    @Test
    public void replacementTest() throws Exception {
        Spacelift.task(StringReplacementTool.class)
                .in(tempFile)
                .replace("o").with("O")
                .execute().await();

        assertThat(readFromFile(tempFile), is(contentAfter1));
    }

    @Test
    public void replacementRegexTest() throws Exception {
        Spacelift.task(StringReplacementTool.class)
                .in(tempFile)
                .replace("[^ \n]").with(".")
                .execute().await();

        assertThat(readFromFile(tempFile), is(contentAfter2));
    }

    @Test
    public void replacementRegexGroupTest() {
        Spacelift.task(StringReplacementTool.class)
                .in(tempFile)
                .replace("(is)").with("$1$1$1")
                .execute().await();

        assertThat(readFromFile(tempFile), is(contentAfter3));
    }

    private String readFromFile(File file) {
        return Spacelift.task(FileSelector.class)
                .select(file)
                .then(FileReader.class)
                .execute().await().values().iterator().next();
    }
}
