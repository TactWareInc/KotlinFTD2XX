**Explanation:**
1. **Kotlin JNI Bridge Code** – The source file where you define your `external` JNI methods.
2. **Gradle Compile** – Your Kotlin/Java code is compiled, producing `.class` files.
3. **javac -h** – This step generates the JNI header (`.h`) from the compiled Java class.
4. **CMake Build** – Use the JNI header to compile and link your native code, producing the shared native library (e.g., `.so` or `.dll`).
5. **Native Library** – The compiled native library is then loaded by your Kotlin/Java application at runtime via JNI.

