plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("androidx.room")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.duihua.chat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.duihua.chat"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file("release.jks") // 你的release签名文件路径
            storePassword = "123456" // keystore密码
            keyAlias = "key0" // 你的密钥别名
            keyPassword = "123456" // 密钥密码
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        // 启用 ViewBinding
        viewBinding = true

        // 启用 DataBinding
        dataBinding = true
    }
    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
        }
    }
    allprojects {
        configurations.all {
            resolutionStrategy {
                force("org.apache.httpcomponents:httpclient:4.5.13")
            }
        }
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("me.majiajie:pager-bottom-tab-strip:2.4.0")
    implementation("com.blankj:utilcodex:1.31.1")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.github.liangjingkanji:StateLayout:1.4.2")
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // 要求OkHttp4以上
    implementation("com.github.liangjingkanji:Net:3.7.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.github.ihsanbal:LoggingInterceptor:3.1.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("io.github.scwang90:refresh-layout-kernel:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-header-classics:3.0.0-alpha")
    implementation("io.github.scwang90:refresh-footer-classics:3.0.0-alpha")

    implementation("com.geyifeng.immersionbar:immersionbar:3.2.2")
    implementation("com.geyifeng.immersionbar:immersionbar-ktx:3.2.2")
    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.4")

    implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-core:2.17.0")
    implementation("com.google.android.exoplayer:exoplayer-dash:2.17.0")
    implementation("com.google.android.exoplayer:exoplayer-hls:2.17.0")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")
    implementation("com.google.android.exoplayer:extension-rtmp:2.17.0")
    implementation("io.reactivex:rxjava:1.1.0")

    implementation("com.github.li-xiaojun:XPopup:2.10.0")

    implementation("io.github.lucksiege:pictureselector:v3.11.2")
    implementation("com.aliyun.dpa:oss-android-sdk:2.9.21")
    implementation(project(":chatinput"))
    implementation(project(":messagelist"))
    val room_version = "2.7.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("com.github.liangjingkanji:soft-input-event:1.0.9")
    implementation("de.hdodenhof:circleimageview:3.1.0")
}