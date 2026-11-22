# Static symbol audit for the Valor Academy GDD package.
# Every Receiver.member token used by the case files and the read-out screens must
# resolve to a declaration somewhere in com.example.game.valor.gdd.
$dir = 'd:\millatary academy\app\src\main\java\com\example\game\valor\gdd'
$ui = 'd:\millatary academy\app\src\main\java\com\example\ui\screens'
$receivers = 'StoryCast|StoryRole|Campaign|StoryDelivery|StoryChoices|Ending|AlignmentState|AlignmentMeter|RankCurve|Prestige|GddMedal|MedalRules|MedalProgress|MedalGallery|WeaponCategories|WeaponStat|StatBars|Gunsmith|Blueprints|GearTable|WeaponHandling|DroneSupport|DroneType|SupportCalls|MissionTypes|CampaignShape|Academy|ReplayModes|MissionMastery|BossRoster|EnemyRoster|EnemyAi|AiState|AiEvent|ControlScheme|AccessibilityOptions|Currencies|UpgradeCategory|SkillTreeSpec|Monetization|WorldMap|WorldSystems|QualityPresets|PerformanceBudget|TestRules|UiScreens|UiScreen|Roadmap|SuccessChecklist|ValorGddAudit|UiScreens'

$sources = @()
$sources += Get-ChildItem $dir -Filter '*Cases.kt'
$sources += Get-ChildItem $ui -Filter 'Gdd*Sections.kt'
$sources += Get-ChildItem $ui -Filter 'ValorGddScreen.kt'

$uses = foreach ($f in $sources) {
    $text = Get-Content $f.FullName -Raw
    foreach ($m in [regex]::Matches($text, "\b($receivers)\.([A-Za-z_][A-Za-z0-9_]*)")) {
        [pscustomobject]@{ Receiver = $m.Groups[1].Value; Member = $m.Groups[2].Value; File = $f.Name }
    }
}

$all = ((Get-ChildItem $dir -Filter '*.kt' | Get-Content -Raw) -join "`n")
$missing = $uses | Sort-Object Receiver, Member -Unique |
    Where-Object { $all -notmatch ("\b" + $_.Member + "\b") }

"receiver.member tokens: " + ($uses | Sort-Object Receiver, Member -Unique).Count
if ($missing) { $missing | Format-Table -AutoSize } else { 'ALL MEMBERS RESOLVE' }
