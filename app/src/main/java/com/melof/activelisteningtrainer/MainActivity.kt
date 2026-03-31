package com.melof.activelisteningtrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.melof.activelisteningtrainer.ui.ChoiceModeScreen
import com.melof.activelisteningtrainer.ui.FeedbackScreen
import com.melof.activelisteningtrainer.ui.GuidedResponseScreen
import com.melof.activelisteningtrainer.ui.PlayMode
import com.melof.activelisteningtrainer.ui.ScenarioListScreen
import com.melof.activelisteningtrainer.ui.SpeakScreen
import com.melof.activelisteningtrainer.ui.theme.ActiveListeningTrainerTheme
import com.melof.activelisteningtrainer.viewmodel.TrainerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ActiveListeningTrainerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val vm: TrainerViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "scenarios") {

                        composable("scenarios") {
                            ScenarioListScreen(
                                vm = vm,
                                onScenarioSelected = { scenario, mode ->
                                    vm.selectScenario(scenario)
                                    when (mode) {
                                        PlayMode.CHOICE  -> navController.navigate("choice")
                                        PlayMode.GUIDED  -> navController.navigate("guided")
                                        PlayMode.FREE    -> navController.navigate("speak")
                                    }
                                }
                            )
                        }

                        composable("choice") {
                            ChoiceModeScreen(
                                vm = vm,
                                onRetry = { navController.popBackStack() },
                                onBack = {
                                    navController.popBackStack("scenarios", inclusive = false)
                                }
                            )
                        }

                        composable("guided") {
                            GuidedResponseScreen(
                                vm = vm,
                                onBack = {
                                    navController.popBackStack("scenarios", inclusive = false)
                                }
                            )
                        }

                        composable("speak") {
                            SpeakScreen(
                                vm = vm,
                                onSubmit = { text ->
                                    vm.submitResponse(text)
                                    navController.navigate("feedback")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("feedback") {
                            FeedbackScreen(
                                vm = vm,
                                onRetry = {
                                    vm.retry()
                                    navController.popBackStack()
                                },
                                onNext = {
                                    navController.popBackStack("scenarios", inclusive = false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
