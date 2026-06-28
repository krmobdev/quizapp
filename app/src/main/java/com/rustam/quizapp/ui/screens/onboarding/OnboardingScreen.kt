package com.rustam.quizapp.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustam.quizapp.R
import com.rustam.quizapp.ui.components.AppBackground

private data class OnboardingPage(
    val emoji: String,
    val titleRes: Int,
    val bodyRes: Int
)

private val pages = listOf(
    OnboardingPage("🎓", R.string.onboarding_page1_title, R.string.onboarding_page1_body),
    OnboardingPage("🎯", R.string.onboarding_page2_title, R.string.onboarding_page2_body),
    OnboardingPage("⚡", R.string.onboarding_page3_title, R.string.onboarding_page3_body),
    OnboardingPage("🪙", R.string.onboarding_page4_title, R.string.onboarding_page4_body)
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val isLast = currentPage == pages.lastIndex

    AppBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button
            Box(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp)
                        .semantics { contentDescription = "Skip onboarding" }
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Page content with slide animation
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    (slideInHorizontally(tween(300)) { it / 2 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { -it / 2 } + fadeOut(tween(200)))
                },
                label = "onboardingPage"
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }

            Spacer(Modifier.weight(1f))

            // Dots indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                pages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentPage) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // Navigation button
            Button(
                onClick = {
                    if (isLast) onFinish() else currentPage++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics {
                        contentDescription = if (isLast) "Start AnimaQuiz" else "Next onboarding page"
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isLast) stringResource(R.string.onboarding_start)
                    else stringResource(R.string.onboarding_next),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = page.emoji,
            fontSize = 80.sp
        )
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(page.bodyRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}
