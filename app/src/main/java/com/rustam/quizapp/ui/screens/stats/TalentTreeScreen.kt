package com.rustam.quizapp.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rustam.quizapp.R
import com.rustam.quizapp.domain.PassiveTalentTree
import com.rustam.quizapp.domain.SkillBonusKind
import com.rustam.quizapp.domain.TalentBranch
import com.rustam.quizapp.domain.TalentNode
import com.rustam.quizapp.domain.TalentTreeState
import com.rustam.quizapp.ui.components.AppDimens
import com.rustam.quizapp.ui.components.GlassCard
import com.rustam.quizapp.ui.components.appTextColor
import com.rustam.quizapp.ui.components.rememberAppThemeColors
import com.rustam.quizapp.ui.theme.CorrectGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalentTreeDialog(
    talentTree: TalentTreeState,
    freeXp: Int,
    onDismiss: () -> Unit,
    onUpgrade: (String) -> Unit
) {
    val colors = rememberAppThemeColors()
    val textColor = appTextColor()
    val progressFraction = talentTree.totalRanks.toFloat() /
        PassiveTalentTree.totalMaxRanks.coerceAtLeast(1)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.talent_tree_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.talent_tree_close)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.CardSpacing)
                ) {
                    item {
                        TalentSummaryCard(
                            talentTree = talentTree,
                            freeXp = freeXp,
                            progressFraction = progressFraction,
                            colors = colors,
                            textColor = textColor
                        )
                    }

                    items(PassiveTalentTree.branches, key = { it.id }) { branch ->
                        TalentBranchCard(
                            branch = branch,
                            talentTree = talentTree,
                            freeXp = freeXp,
                            colors = colors,
                            textColor = textColor,
                            onUpgrade = onUpgrade
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TalentSummaryCard(
    talentTree: TalentTreeState,
    freeXp: Int,
    progressFraction: Float,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color
) {
    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(
                            R.string.talent_tree_progress,
                            talentTree.totalRanks,
                            PassiveTalentTree.totalMaxRanks
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.65f)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.char_free_xp_balance, freeXp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                strokeCap = StrokeCap.Round,
                trackColor = colors.progressTrack,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.talent_tree_hint),
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp,
                color = textColor.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
private fun TalentBranchCard(
    branch: TalentBranch,
    talentTree: TalentTreeState,
    freeXp: Int,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    textColor: androidx.compose.ui.graphics.Color,
    onUpgrade: (String) -> Unit
) {
    val branchNodes = PassiveTalentTree.nodes.filter { it.branch == branch }
    val branchRanks = branchNodes.sumOf { talentTree.rank(it) }
    val branchMax = branchNodes.size * PassiveTalentTree.MAX_RANK
    val branchBonus = branchRanks * branch.perRank
    val branchComplete = branchRanks >= branchMax
    val visibleNodes = visibleBranchNodes(branch, talentTree)
    val hiddenCount = branchNodes.size - visibleNodes.size

    GlassCard(colors = colors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = branch.emoji, fontSize = 20.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(branch.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = branchBonusText(branch.kind, branchBonus),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    )
                }
                Text(
                    text = "$branchRanks/$branchMax",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (branchComplete) CorrectGreen else textColor.copy(alpha = 0.7f)
                )
            }

            LinearProgressIndicator(
                progress = { branchRanks / branchMax.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                strokeCap = StrokeCap.Round,
                trackColor = colors.progressTrack,
                color = if (branchComplete) CorrectGreen else MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = textColor.copy(alpha = 0.06f))

            visibleNodes.forEachIndexed { index, node ->
                val isLast = index == visibleNodes.lastIndex
                TalentNodeRow(
                    node = node,
                    rank = talentTree.rank(node),
                    freeXp = freeXp,
                    talentTree = talentTree,
                    textColor = textColor,
                    colors = colors,
                    showConnectorBelow = !isLast || hiddenCount > 0,
                    onUpgrade = { onUpgrade(node.id) }
                )
            }

            if (hiddenCount > 0) {
                HiddenNodesHint(count = hiddenCount, textColor = textColor)
            }
        }
    }
}

@Composable
private fun TalentNodeRow(
    node: TalentNode,
    rank: Int,
    freeXp: Int,
    talentTree: TalentTreeState,
    textColor: androidx.compose.ui.graphics.Color,
    colors: com.rustam.quizapp.ui.components.AppThemeColors,
    showConnectorBelow: Boolean,
    onUpgrade: () -> Unit
) {
    val maxed = rank >= node.maxRank
    val locked = !PassiveTalentTree.isUnlocked(node, talentTree)
    val cost = PassiveTalentTree.nextXpCost(node, talentTree)
    val canUpgrade = PassiveTalentTree.canUpgrade(node, talentTree, freeXp)
    val perRankText = branchBonusText(node.branch.kind, node.branch.perRank)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (locked) 0.55f else 1f),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TimelineIndicator(
            maxed = maxed,
            locked = locked,
            active = canUpgrade && !locked,
            showLineBelow = showConnectorBelow,
            trackColor = colors.progressTrack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (showConnectorBelow) 6.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.talent_node_short, node.depth + 1),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = "$rank/${node.maxRank}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            if (!locked) {
                LinearProgressIndicator(
                    progress = { rank / node.maxRank.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = colors.progressTrack,
                    color = if (maxed) CorrectGreen else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.talent_node_bonus, perRankText),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = stringResource(R.string.talent_node_locked),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }

        when {
            maxed -> {
                Surface(
                    color = CorrectGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(width = 52.dp, height = 36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = CorrectGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            locked -> {
                Surface(
                    color = textColor.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(width = 52.dp, height = 36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = textColor.copy(alpha = 0.35f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            else -> {
                FilledTonalButton(
                    onClick = onUpgrade,
                    enabled = canUpgrade,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .width(72.dp)
                        .height(36.dp)
                ) {
                    Text(
                        text = stringResource(R.string.talent_upgrade_xp, cost),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineIndicator(
    maxed: Boolean,
    locked: Boolean,
    active: Boolean,
    showLineBelow: Boolean,
    trackColor: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    when {
                        maxed -> CorrectGreen
                        active -> MaterialTheme.colorScheme.primary
                        locked -> trackColor
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (maxed) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(8.dp)
                )
            }
        }
        if (showLineBelow) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(
                        if (locked) trackColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
            )
        }
    }
}

@Composable
private fun HiddenNodesHint(
    count: Int,
    textColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 34.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.3f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = stringResource(R.string.talent_nodes_hidden, count),
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.45f)
        )
    }
}

private fun visibleBranchNodes(branch: TalentBranch, state: TalentTreeState): List<TalentNode> {
    val nodes = PassiveTalentTree.nodes.filter { it.branch == branch }
    val actionableIndex = nodes.indexOfFirst { node ->
        PassiveTalentTree.isUnlocked(node, state) && state.rank(node) < PassiveTalentTree.MAX_RANK
    }
    return when {
        actionableIndex == -1 -> nodes
        else -> nodes.take((actionableIndex + 2).coerceAtMost(nodes.size))
    }
}

@Composable
private fun branchBonusText(kind: SkillBonusKind, bonus: Float): String {
    val res = PassiveTalentTree.bonusDescriptionRes(kind)
    return when (kind) {
        SkillBonusKind.EXTRA_TIME -> stringResource(res, bonus)
        SkillBonusKind.XP_PERCENT,
        SkillBonusKind.COIN_PERCENT,
        SkillBonusKind.CRIT_CHANCE -> stringResource(res, bonus)
        else -> stringResource(res, bonus.toInt())
    }
}