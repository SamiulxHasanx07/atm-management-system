# Bangla Bank ATM - Frontend Build & Configuration Guide

This document outlines the necessary steps to compile the frontend codebase into a standalone executable ( `.exe`) and how to properly configure the dynamic backend API connection.

## 🛠️ How to Build the Application

If you make changes to the frontend Java code, FXML layouts, or CSS files, you will need to recompile the project into a new `.exe` application. 

> [!NOTE]
> Ensure you are running these commands in your Git Bash terminal and that you are located inside the `G:\atm-management-system\frontend` folder.

### Step 1: Compile Code & Build Java Runtime Image
This command uses Maven to clean out old build artifacts, compile your new code, and package it together with a custom Java runtime (so the user doesn't need to have Java installed on their PC).

```bash
export JAVA_HOME="C:\Program Files\Java\jdk-25"
./mvnw.cmd compile javafx:jlink
```

### Step 2: Generate the Standalone `.exe`
Once the code is compiled, use Java's `jpackage` utility to bundle the compiled code and the custom runtime into a portable Windows executable application folder.

```bash
export JAVA_HOME="C:\Program Files\Java\jdk-25"
jpackage --type app-image --dest target/BanglaBank_Builds --name "BanglaBankATM" --module com.example.atmmanagementsystem/com.example.atmmanagementsystem.BanglaBankApplication --runtime-image target/app
```

> [!TIP]  
> After running this command, your new application will be waiting for you inside `frontend\target\BanglaBank_Builds\BanglaBankATM`. You can zip this folder up to share it with anyone!
> **If you ever get an error that the destination already exists**, simply change `--dest target/BanglaBank_Builds` to `--dest target/BanglaBank_Build2` (or similar) to force it to use a fresh folder!

---

## 🔗 How to Change the Backend URL

Because the application uses a compiled, hardcoded backend URL, you must edit the source code if you ever need to change the server address in the future.

1. Open `G:\atm-management-system\frontend\src\main\java\com\example\atmmanagementsystem\api\ApiConfig.java` in any code editor (like VS Code or Notepad).
2. Locate this line of code at the top of the file:
   ```java
   public static final String BASE_URL = "https://atm-management-system-backend.vercel.app/api";
   ```
3. Change the URL text to match your new hosted backend.
4. Save the file.
5. Follow **Step 1** and **Step 2** from the guide above to recompile your brand new `.exe` containing your updated URL!
