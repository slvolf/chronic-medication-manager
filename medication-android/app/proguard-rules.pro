# ProGuard 混淆规则（MVP阶段暂不混淆）
-keepattributes *Annotation*
-keep class com.medication.manage.model.** { *; }
-keep class com.medication.manage.api.** { *; }
