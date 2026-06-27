<?xml version="1.0" encoding="UTF-8"?>

<project>

    <name>Matrix Camera</name>

    <tagline>
        Live Nothing-inspired Matrix Camera for Nothing Phone and CMF devices.
    </tagline>

    <goal>
        Build a premium Android application inspired by Nothing OS.
        The application provides a live Matrix-style front camera experience.

        The application should not be a traditional camera app.

        It should feel like a native Nothing utility that users launch instantly
        from a minimal homescreen widget.
    </goal>

    <platform>

        <os>Android 14+</os>
        <language>Kotlin</language>
        <ui>Jetpack Compose</ui>
        <camera>CameraX</camera>
        <ml>ML Kit Face Detection</ml>
        <min_sdk>26</min_sdk>
        <target_sdk>35</target_sdk>

    </platform>

    <design_language>

        <style>
            Nothing OS inspired.
        </style>

        <theme>

            Pure black backgrounds.

            White matrix dots.

            Nothing Sans typography.

            Large rounded corners.

            Minimal animations.

            Premium spacing.

            Zero unnecessary UI.

            High quality blur.

            Glass effects where appropriate.

            Smooth transitions.

        </theme>

    </design_language>

    <homescreen_widget>

        <size>2x2</size>

        <purpose>

            Widget is NOT a live camera.

            Widget acts as an instant launcher.

            Widget should display:

            • Matrix icon

            • Current style

            • Last generated avatar (optional)

            • Animated breathing effect

        </purpose>

        <interaction>

            Single Tap

            →

            Launch Matrix Camera Activity instantly.

        </interaction>

    </homescreen_widget>

    <launch_experience>

        User taps widget.

        Immediately open full screen.

        Fade animation.

        Black screen.

        Matrix loading animation.

        Camera initializes.

        Matrix rendering begins.

        Total startup time should feel below one second.

    </launch_experience>

    <camera>

        <lens>Front Camera</lens>

        <library>CameraX</library>

        <fps>30</fps>

        <preview>

            Hide the normal camera preview.

            User should only see the Matrix version.

        </preview>

    </camera>

    <face_detection>

        Detect largest visible face.

        Track continuously.

        Detect:

        Smile

        Blink

        Eyes

        Mouth

        Head rotation

        Face center

        Face size

        Keep tracking smooth.

        No flickering.

    </face_detection>

    <matrix_renderer>

        Convert every frame into:

        Matrix pixel art.

        Steps:

        Camera Frame

        →

        Grayscale

        →

        Contrast enhancement

        →

        Edge detection

        →

        Threshold

        →

        Downscale

        →

        Matrix dots

        →

        Animated rendering

    </matrix_renderer>

    <matrix_styles>

        <style>

            Nothing Matrix

            White circular dots.

        </style>

        <style>

            LED Matrix

            Circular LEDs.

        </style>

        <style>

            ASCII Terminal

            Characters instead of pixels.

        </style>

        <style>

            Dot Glyph

            Inspired by Nothing Glyph Matrix.

        </style>

        <style>

            Retro LCD

        </style>

        <style>

            Pixel Game Boy

        </style>

    </matrix_styles>

    <animations>

        Smooth matrix refresh.

        Dot fade.

        Dot pulse.

        Matrix rain.

        Ripple animation.

        Face scanning animation.

        Boot animation.

        Loading scan line.

        Capture flash.

    </animations>

    <controls>

        Bottom Floating Controls.

        Capture.

        Video.

        Style Switcher.

        Brightness.

        Mirror.

        Flash (future).

        Settings.

    </controls>

    <capture>

        Save:

        PNG

        Transparent PNG

        JPG

        GIF

        MP4

    </capture>

    <share>

        Share directly to:

        Instagram

        WhatsApp

        Telegram

        X

        Discord

    </share>

    <avatar_generator>

        One click.

        Generate:

        Circular avatar.

        Square avatar.

        Wallpaper.

        Contact photo.

        Profile image.

    </avatar_generator>

    <performance>

        Maintain 30 FPS minimum.

        Memory optimized.

        Bitmap reuse.

        Coroutine processing.

        GPU accelerated rendering where possible.

        Avoid unnecessary allocations.

        No battery drain while closed.

    </performance>

    <privacy>

        Camera starts only after user opens app.

        Camera stops immediately when app closes.

        No background camera.

        No foreground service.

        No network access required.

        Entire processing happens locally.

    </privacy>

    <future_features>

        Gesture control.

        Smile trigger.

        Blink capture.

        Voice capture.

        AI portrait enhancement.

        Matrix stickers.

        Matrix filters.

        Lock screen shortcut.

        Quick Settings tile.

        Dynamic Nothing Widget.

        Wear OS companion.

    </future_features>

    <branding>

        App Name:

        Matrix Camera

        Alternative Names:

        Nothing Matrix

        DotCam

        Matrix Lens

        Dot Vision

        Matrix Studio

    </branding>

    <developer_notes>

        The application should not look like a clone of the Nothing Camera.

        Instead, it should feel like an official experimental Nothing Labs application.

        Every interaction should be smooth.

        Every animation should be subtle.

        Every screen should embrace minimalism.

        The experience should prioritize speed, elegance, and simplicity over excessive features.

    </developer_notes>

</project>
