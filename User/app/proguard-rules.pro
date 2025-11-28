# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/bowshulsheikrahaman/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
-keep class android.support.v7.widget.** { *; }

-ignorewarnings

#-keep class * {
#    public private *;
#}

#-keep public class

# Butterknife
-dontwarn butterknife.internal.**
-keep class butterknife.** { *; }
-keep class **$$ViewInjector { *; }
-keep class android.support.graphics.** { *; }
-keep class android.support.animation.** { *; }
-keep class android.animation.** { *; }
-keep class android.graphics.** { *; }
-keep class android.view.animation.** { *; }

-keepclasseswithmembernames class * {
    @butterknife.BindView <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.OnClick <methods>;
    @butterknife.OnEditorAction <methods>;
    @butterknife.OnItemClick <methods>;
    @butterknife.OnItemLongClick <methods>;
    @butterknife.OnLongClick <methods>;
}

#For Stripe payment (card payment)
-keep class com.stripe.android.** { *; }


#For retrofit
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-keepattributes *Annotation*,SourceFile,LineNumberTable

# Support Design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
-keep class android.support.v7.widget.AppCompatImageView.** { *; }
-dontwarn com.github.mikephil.**
-keep public class com.rideincab.user.common.helper.** {
     public protected *;
}

-keep class com.rideincab.user.taxi.views.firebaseChat.FirebaseChatModelClass{ *; }

-keep class com.rideincab.user.taxi.views.main.MainActivity{ *; }
-keep class com.rideincab.user.common.datamodels.**{ *; }
-keep class com.rideincab.user.taxi.datamodels.**{ *; }
-keep class com.rideincab.user.common.map.**{ *; }

-keep class com.rideincab.user.taxi.views.voip.CabmeSinchService.** { *; }
#}



-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.**
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.**

-keep class com.firebase.geofire.**{*;}
-keep class com.google.firebase.database.GenericTypeIndicator{*;}
-keepattributes Signature

# This rule will properly ProGuard all the model classes in the package directory.
# Replace models with your folder of database model classes.
-keepclassmembers class your.package.name.models.** {
  *;
}

# General Firebase rules
-keepattributes Signature
-keepattributes *Annotation*

# Keep Firebase classes and members required for reflection
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**


#-dontobfuscate
#-printmapping mapping.txt



-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.**
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.**
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response


# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Prevent R8 from leaving Data object members always null
-keepclasseswithmembers class * {
    <init>(...);
    @com.google.gson.annotations.SerializedName <fields>;
}
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.maps.android.** { *; }
-dontwarn com.google.android.gms.maps.**
-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}
#
## Keep no-args constructor of classes which can be used with @JsonAdapter
## By default their no-args constructor is invoked to create an adapter instance
#-keepclassmembers class * extends com.google.gson.TypeAdapter {
#  <init>();
#}
#-keepclassmembers class * implements com.google.gson.TypeAdapterFactory {
#  <init>();
#}
#-keepclassmembers class * implements com.google.gson.JsonSerializer {
#  <init>();
#}
#-keepclassmembers class * implements com.google.gson.JsonDeserializer {
#  <init>();
#}
#
## Keep fields annotated with @SerializedName for classes which are referenced.
## If classes with fields annotated with @SerializedName have a no-args
## constructor keep that as well. Based on
## https://issuetracker.google.com/issues/150189783#comment11.
## See also https://github.com/google/gson/pull/2420#discussion_r1241813541
## for a more detailed explanation.
#-if class *
#-keepclasseswithmembers,allowobfuscation class <1> {
#  @com.google.gson.annotations.SerializedName <fields>;
#}
#-if class * {
#  @com.google.gson.annotations.SerializedName <fields>;
#}
#-keepclassmembers,allowobfuscation,allowoptimization class <1> {
#  <init>();
#}
