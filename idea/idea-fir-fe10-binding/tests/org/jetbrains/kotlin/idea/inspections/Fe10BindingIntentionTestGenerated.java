/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.inspections;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/testData/intentions/conventionNameCalls/replaceContains")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class Fe10BindingIntentionTestGenerated extends AbstractFe10BindingIntentionTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInReplaceContains() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("idea/testData/intentions/conventionNameCalls/replaceContains"), Pattern.compile("^([\\w\\-_]+)\\.(kt|kts)$"), null, true);
    }

    @TestMetadata("containsFromJava.kt")
    public void testContainsFromJava() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/containsFromJava.kt");
    }

    @TestMetadata("containsInExpression.kt")
    public void testContainsInExpression() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/containsInExpression.kt");
    }

    @TestMetadata("extensionFunction.kt")
    public void testExtensionFunction() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/extensionFunction.kt");
    }

    @TestMetadata("functionLiteralArgument.kt")
    public void testFunctionLiteralArgument() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/functionLiteralArgument.kt");
    }

    @TestMetadata("functionLiteralArgumentAfterSemicolon.kt")
    public void testFunctionLiteralArgumentAfterSemicolon() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/functionLiteralArgumentAfterSemicolon.kt");
    }

    @TestMetadata("functionLiteralArgumentAtStartOfBlock.kt")
    public void testFunctionLiteralArgumentAtStartOfBlock() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/functionLiteralArgumentAtStartOfBlock.kt");
    }

    @TestMetadata("functionLiteralArgumentInExpression.kt")
    public void testFunctionLiteralArgumentInExpression() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/functionLiteralArgumentInExpression.kt");
    }

    @TestMetadata("invalidArgument.kt")
    public void testInvalidArgument() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/invalidArgument.kt");
    }

    @TestMetadata("missingArgument.kt")
    public void testMissingArgument() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/missingArgument.kt");
    }

    @TestMetadata("missingDefaultArgument.kt")
    public void testMissingDefaultArgument() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/missingDefaultArgument.kt");
    }

    @TestMetadata("multipleArguments.kt")
    public void testMultipleArguments() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/multipleArguments.kt");
    }

    @TestMetadata("notContains.kt")
    public void testNotContains() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/notContains.kt");
    }

    @TestMetadata("qualifier.kt")
    public void testQualifier() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/qualifier.kt");
    }

    @TestMetadata("simpleArgument.kt")
    public void testSimpleArgument() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/simpleArgument.kt");
    }

    @TestMetadata("simpleStringLiteral.kt")
    public void testSimpleStringLiteral() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/simpleStringLiteral.kt");
    }

    @TestMetadata("super.kt")
    public void testSuper() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/super.kt");
    }

    @TestMetadata("twoArgsContainsFromJava.kt")
    public void testTwoArgsContainsFromJava() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/twoArgsContainsFromJava.kt");
    }

    @TestMetadata("typeArguments.kt")
    public void testTypeArguments() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/typeArguments.kt");
    }

    @TestMetadata("unacceptableVararg1.kt")
    public void testUnacceptableVararg1() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/unacceptableVararg1.kt");
    }

    @TestMetadata("unacceptableVararg2.kt")
    public void testUnacceptableVararg2() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/unacceptableVararg2.kt");
    }

    @TestMetadata("withoutOperatorModifier.kt")
    public void testWithoutOperatorModifier() throws Exception {
        runTest("idea/testData/intentions/conventionNameCalls/replaceContains/withoutOperatorModifier.kt");
    }
}