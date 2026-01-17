#!/bin/bash

# OS Kernel Simulation - Build & Run Script
# This script compiles and runs the OS kernel simulation

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         OS Kernel Simulation - Build & Run Script          ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Step 1: Set Java environment
echo "📦 Setting up Java environment..."
export JAVA_HOME=/Users/vutl2004/Downloads/jdk-25.0.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

# Step 2: Verify Java installation
echo "✓ Java version:"
java -version
echo ""

# Step 3: Navigate to project directory
echo "📁 Navigating to project directory..."
cd /Users/vutl2004/Documents/OS
echo "✓ Current directory: $(pwd)"
echo ""

# Step 4: Create bin directory
echo "📂 Creating bin directory for compiled files..."
mkdir -p bin
echo "✓ Directory created: bin/"
echo ""

# Step 5: Compile all Java files
echo "🔨 Compiling Java source files..."
echo "   Compiling: process classes..."
javac -d bin src/process/ProcessState.java src/process/Process.java src/process/Thread.java
if [ $? -eq 0 ]; then
    echo "   ✓ Process classes compiled"
else
    echo "   ✗ Error compiling process classes"
    exit 1
fi

echo "   Compiling: scheduling classes..."
javac -d bin src/scheduling/Scheduler.java
if [ $? -eq 0 ]; then
    echo "   ✓ Scheduler class compiled"
else
    echo "   ✗ Error compiling scheduler"
    exit 1
fi

echo "   Compiling: dispatcher classes..."
javac -d bin src/dispatcher/Dispatcher.java
if [ $? -eq 0 ]; then
    echo "   ✓ Dispatcher class compiled"
else
    echo "   ✗ Error compiling dispatcher"
    exit 1
fi

echo "   Compiling: kernel classes..."
javac -d bin src/kernel/OSKernel.java
if [ $? -eq 0 ]; then
    echo "   ✓ Kernel class compiled"
else
    echo "   ✗ Error compiling kernel"
    exit 1
fi

echo "   Compiling: simulation main..."
javac -d bin src/OSSimulation.java
if [ $? -eq 0 ]; then
    echo "   ✓ Simulation main compiled"
else
    echo "   ✗ Error compiling simulation"
    exit 1
fi

echo ""
echo "✅ All source files compiled successfully!"
echo ""

# Step 6: Run the simulation
echo "🚀 Starting OS Kernel Simulation..."
echo "════════════════════════════════════════════════════════════"
echo ""

java -cp bin OSSimulation

echo ""
echo "════════════════════════════════════════════════════════════"
echo "✅ Simulation completed successfully!"
echo ""
echo "📊 Next steps:"
echo "   1. Review the execution output above"
echo "   2. Open dashboard.html in your browser:"
echo "      open /Users/vutl2004/Documents/OS/dashboard.html"
echo "   3. Read documentation:"
echo "      • README.md - Complete documentation"
echo "      • QUICK_REFERENCE.md - Quick start guide"
echo "      • CLASS_DIAGRAM.txt - Architecture diagram"
echo "      • docs/SOLARIS_DOCUMENTATION.md - OS concepts"
echo ""
