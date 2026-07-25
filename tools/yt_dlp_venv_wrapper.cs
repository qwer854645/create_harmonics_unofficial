using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Threading;

/// <summary>
/// Drop-in yt-dlp.exe for Create: Resonance (works with stock jars).
/// Runs: venv\Scripts\python.exe -m yt_dlp [args]
/// so Bilibili patches in the venv apply. Avoids the tiny venv Scripts launcher
/// which breaks when the game directory contains '&'.
/// </summary>
internal static class Program
{
    private static int Main(string[] args)
    {
        try
        {
            var exeDir = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
            if (string.IsNullOrEmpty(exeDir))
            {
                Console.Error.WriteLine("yt-dlp wrapper: cannot resolve install directory");
                return 1;
            }

            var python = Path.Combine(exeDir, "venv", "Scripts", "python.exe");
            if (!File.Exists(python))
            {
                python = Path.Combine(exeDir, "venv", "bin", "python");
            }

            if (File.Exists(python))
            {
                return RunForwarding(python, PrependModule(args));
            }

            var official = Path.Combine(exeDir, "yt-dlp.exe.official");
            if (File.Exists(official))
            {
                return RunForwarding(official, args);
            }

            Console.Error.WriteLine("yt-dlp wrapper: missing venv\\Scripts\\python.exe (and no yt-dlp.exe.official)");
            return 1;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine("yt-dlp wrapper: " + ex.Message);
            return 1;
        }
    }

    private static string[] PrependModule(string[] args)
    {
        var result = new string[args.Length + 2];
        result[0] = "-m";
        result[1] = "yt_dlp";
        Array.Copy(args, 0, result, 2, args.Length);
        return result;
    }

    private static int RunForwarding(string fileName, string[] args)
    {
        var psi = new ProcessStartInfo
        {
            FileName = fileName,
            Arguments = EscapeArgs(args),
            UseShellExecute = false,
            CreateNoWindow = true,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            RedirectStandardInput = true,
        };

        using (var proc = Process.Start(psi))
        {
            if (proc == null)
            {
                Console.Error.WriteLine("yt-dlp wrapper: failed to start " + fileName);
                return 1;
            }

            // Forward pipes so Minecraft ProcessBuilder captures yt-dlp JSON / errors.
            var stdoutDone = new ManualResetEvent(false);
            var stderrDone = new ManualResetEvent(false);

            ThreadPool.QueueUserWorkItem(_ =>
            {
                try { Copy(proc.StandardOutput.BaseStream, Console.OpenStandardOutput()); }
                finally { stdoutDone.Set(); }
            });
            ThreadPool.QueueUserWorkItem(_ =>
            {
                try { Copy(proc.StandardError.BaseStream, Console.OpenStandardError()); }
                finally { stderrDone.Set(); }
            });

            // Parent rarely writes stdin for -j; close so child is not blocked.
            try { proc.StandardInput.Close(); } catch { }

            proc.WaitForExit();
            stdoutDone.WaitOne();
            stderrDone.WaitOne();
            return proc.ExitCode;
        }
    }

    private static void Copy(Stream input, Stream output)
    {
        var buffer = new byte[8192];
        int read;
        while ((read = input.Read(buffer, 0, buffer.Length)) > 0)
        {
            output.Write(buffer, 0, read);
            output.Flush();
        }
    }

    private static string EscapeArgs(string[] args)
    {
        if (args == null || args.Length == 0) return string.Empty;
        var parts = new string[args.Length];
        for (var i = 0; i < args.Length; i++)
            parts[i] = Quote(args[i] ?? string.Empty);
        return string.Join(" ", parts);
    }

    private static string Quote(string arg)
    {
        if (arg.Length == 0) return "\"\"";
        var needsQuotes = false;
        for (var i = 0; i < arg.Length; i++)
        {
            var c = arg[i];
            if (c == ' ' || c == '\t' || c == '\n' || c == '\v' || c == '"')
            {
                needsQuotes = true;
                break;
            }
        }
        if (!needsQuotes) return arg;

        var sb = new System.Text.StringBuilder();
        sb.Append('"');
        var backslashes = 0;
        for (var i = 0; i < arg.Length; i++)
        {
            var c = arg[i];
            if (c == '\\')
            {
                backslashes++;
            }
            else if (c == '"')
            {
                sb.Append('\\', backslashes * 2 + 1);
                sb.Append('"');
                backslashes = 0;
            }
            else
            {
                sb.Append('\\', backslashes);
                sb.Append(c);
                backslashes = 0;
            }
        }
        sb.Append('\\', backslashes * 2);
        sb.Append('"');
        return sb.ToString();
    }
}
