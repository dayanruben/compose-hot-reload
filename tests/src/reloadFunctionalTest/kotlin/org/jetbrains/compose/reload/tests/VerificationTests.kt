/*
 * Copyright 2024-2025 JetBrains s.r.o. and Compose Hot Reload contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package org.jetbrains.compose.reload.tests

import org.jetbrains.compose.reload.orchestration.OrchestrationMessage
import org.jetbrains.compose.reload.test.gradle.HotReloadTest
import org.jetbrains.compose.reload.test.gradle.HotReloadTestFixture
import org.jetbrains.compose.reload.test.gradle.checkScreenshot
import org.jetbrains.compose.reload.test.gradle.initialSourceCode
import org.jetbrains.compose.reload.test.gradle.replaceText
import org.jetbrains.compose.reload.utils.QuickTest
import org.junit.jupiter.api.Disabled
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class VerificationTests {

    @HotReloadTest
    @QuickTest
    fun `illegal code change - update compose entry function directly`(fixture: HotReloadTestFixture) = fixture.runTest {
        val d = "$"
        val code = fixture.initialSourceCode(
            """
                import org.jetbrains.compose.reload.test.*
                
                fun main() {
                    screenshotTestApplication {
                        TestText("Foo")
                    }
                }
            """.trimIndent()
        )

        fixture.checkScreenshot("0-initial")

        fixture.runTransaction {
            /*
            Legal Change: replace the code inside the composable function
            */
            code.replaceText("""TestText("Foo")""", """TestText("Bar")""")
            requestAndAwaitReload()
            fixture.checkScreenshot("1-correct-change")
        }

        fixture.runTransaction {
            /*
            Illegal Change: replace the code outside composable scope
            */
            code.replaceText(
                """
                fun main() {
                    screenshotTestApplication {
                        TestText("Bar")
                    }
                }""".trimIndent(),
                """
                fun main() {
                    var myVariable = 0
                    screenshotTestApplication {
                        myVariable = 1
                        TestText("Bar ${d}myVariable")
                    }
                }""".trimIndent()
            )
            requestReload()
            val request = skipToMessage<OrchestrationMessage.ReloadClassesRequest>()
            val result = skipToMessage<OrchestrationMessage.ReloadClassesResult>()
            assertEquals(request.messageId, result.reloadRequestId)
            assertFalse(result.isSuccess)
            assertEquals(
                "Compose Hot Reload does not support the redefinition of the Compose entry method." +
                    " Please restart the App or revert the changes in 'MainKt.main ()V'.",
                result.errorMessage
            )
        }
    }

    @HotReloadTest
    @QuickTest
    fun `multiple file change restored correctly`(fixture: HotReloadTestFixture) = fixture.runTest {
        val d = "\$"
        val code = fixture.initialSourceCode(
            """
                import androidx.compose.foundation.layout.Column
                import androidx.compose.runtime.Composable
                import org.jetbrains.compose.reload.test.*
                
                object Foo {
                    @Composable
                    fun foo() {
                        TestText("Foo text")
                    }
                }
                
                fun main() {
                    screenshotTestApplication {
                        Column {
                            TestText("Hello")
                            Foo.foo()
                        }
                    }
                }
            """.trimIndent()
        )

        fixture.checkScreenshot("0-initial")

        fixture.runTransaction {
            /*
            Illegal Change: replace the code inside the compose entry function
            */
            code.replaceText("""fun main() {""", """fun main() { val a = 10""")
            code.replaceText("""TestText("Hello")""", """TestText("Hello ${d}a")""")
            /*
            Legal Change: change code in composable function
            */
            code.replaceText("""TestText("Foo text")""", """TestText("Foo")""")
            requestReload()
            val request = skipToMessage<OrchestrationMessage.ReloadClassesRequest>()
            val result = skipToMessage<OrchestrationMessage.ReloadClassesResult>()
            assertEquals(request.messageId, result.reloadRequestId)
            assertFalse(result.isSuccess)
        }

        fixture.runTransaction {
            /*
            Revert the illegal change
            */
            code.replaceText("""fun main() { val a = 10""", """fun main() {""")
            code.replaceText("""TestText("Hello ${d}a")""", """TestText("Hello")""")
            requestAndAwaitReload()
            fixture.checkScreenshot("2-restored-state")
        }
    }

    @HotReloadTest
    @QuickTest
    fun `illegal code change - change local variables in main`(fixture: HotReloadTestFixture) = fixture.runTest {
        val code = fixture.initialSourceCode(
            """
                import org.jetbrains.compose.reload.test.*
                
                fun main() {
                    val str1 = "Hello "
                    screenshotTestApplication {
                        TestText(str1 + "Foo!")
                    }
                }
            """.trimIndent()
        )

        fixture.checkScreenshot("0-initial")

        fixture.runTransaction {
            /*
            Legal Change: replace the code inside the composable function
            */
            code.replaceText(""""Foo!"""", """"Bar!"""")
            requestAndAwaitReload()
            fixture.checkScreenshot("1-correct-change")
        }

        fixture.runTransaction {
            /*
            Illegal Change: change local variable name in main
            */
            code.replaceText("str1", "str2")
            requestReload()
            val request = skipToMessage<OrchestrationMessage.ReloadClassesRequest>()
            val result = skipToMessage<OrchestrationMessage.ReloadClassesResult>()
            assertEquals(request.messageId, result.reloadRequestId)
            assertFalse(result.isSuccess)
            assertEquals(
                "Compose Hot Reload does not support the redefinition of the Compose entry method." +
                    " Please restart the App or revert the changes in 'MainKt.main ()V'.",
                result.errorMessage
            )
        }
    }

    @HotReloadTest
    @QuickTest
    @Disabled("Current verification diagnostic does not support such cases")
    fun `illegal code change - update compose entry function transitively`(fixture: HotReloadTestFixture) = fixture.runTest {
        val code = fixture.initialSourceCode(
            """
            import androidx.compose.runtime.Composable
            import org.jetbrains.compose.reload.test.*

            fun foo2(a: @Composable () -> Unit) {
                screenshotTestApplication {
                    a()
                }
            }
            
            fun foo(a: @Composable () -> Unit) {
                foo2(a)
            }
            
            fun main() {
                foo {
                    TestText("Foo")
                }
            }
            """.trimIndent()
        )

        fixture.checkScreenshot("0-initial")

        fixture.runTransaction {
            /*
            Legal Change: replace the code inside the composable function
            */
            code.replaceText("""TestText("Foo")""", """TestText("Bar")""")
            requestAndAwaitReload()
            fixture.checkScreenshot("1-change-inside-composable")
        }

        fixture.runTransaction {
            /*
            Legal Change: replace the code inside the composable function
            */
            code.replaceText("""foo2(a)""", """
                val b = 10
                foo2(a)
            """.trimIndent())
            requestAndAwaitReload()
            fixture.checkScreenshot("2-change-outside-compose")
        }

        fixture.runTransaction {
            /*
            Illegal Change: replace the code outside composable scope
            */
            code.replaceText(
                """
                fun main() {
                    foo {
                        TestText("Foo")
                    }
                }""".trimIndent(),
                """
                fun main() {
                    val a = 10
                    foo {
                        TestText("Foo")
                    }
                }""".trimIndent()
            )
            requestReload()
            val request = skipToMessage<OrchestrationMessage.ReloadClassesRequest>()
            val result = skipToMessage<OrchestrationMessage.ReloadClassesResult>()
            assertEquals(request.messageId, result.reloadRequestId)
            assertFalse(result.isSuccess)
            assertEquals(
                "Compose Hot Reload does not support the redefinition of the Compose entry method." +
                    " Please restart the App or revert the changes in 'MainKt.main ()V'.",
                result.errorMessage
            )
        }
    }
}
