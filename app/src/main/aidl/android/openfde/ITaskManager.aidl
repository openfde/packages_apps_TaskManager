package android.openfde;

interface ITaskManager {
    String getTasks();
    void killTaskByPid(int pid);
    String getIconB64ByTaskName(String taskName);
}