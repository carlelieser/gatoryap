_default:
    @just --list

# Start Postgres and wait until it accepts connections
db-up:
    docker compose up -d
    @printf "waiting for postgres"
    @for i in $(seq 1 60); do \
        if docker exec gatoryap-postgres pg_isready -U gatoryap -d gatoryap >/dev/null 2>&1; then \
            printf " ready\n"; exit 0; \
        fi; \
        printf "."; sleep 1; \
    done; \
    printf " timed out\n"; exit 1

# Stop Postgres, keeping data
db-down:
    docker compose down

# Stop Postgres and delete all local data
db-reset:
    docker compose down -v
    @just db-up

# Open a psql shell
db-shell:
    docker exec -it gatoryap-postgres psql -U gatoryap -d gatoryap

# Run the server (starts Postgres first; migrations run on boot)
run: db-up
    ./gradlew :server:run

# Compile every module
build:
    ./gradlew build

# Run tests
test:
    ./gradlew test

# Build the Android APK
android:
    ./gradlew :app:androidApp:assembleDebug

# Link the iOS framework for the simulator
ios-framework:
    ./gradlew :app:composeApp:linkDebugFrameworkIosSimulatorArm64

# Regenerate iosApp.xcodeproj from project.yml
ios-project:
    cd app/iosApp && xcodegen generate

# Build the iOS app (regenerates the Xcode project first)
ios: ios-project
    cd app/iosApp && xcodebuild -project iosApp.xcodeproj -scheme iosApp \
        -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
        -configuration Debug build

# Open the iOS project in Xcode
ios-open: ios-project
    open app/iosApp/iosApp.xcodeproj

# Remove build outputs
clean:
    ./gradlew clean

# Check the server is healthy
health:
    @curl -s http://localhost:8080/healthz && echo
    @curl -s http://localhost:8080/readyz && echo
