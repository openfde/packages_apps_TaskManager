package android.openfde;

interface ITaskManager {
    String listTasksPid();
    String getTaskInfoByPid(int pid);
    void killTaskByPid(int pid);
    String getIconB64ByTaskName(String taskName);
}