package com.geekbrains.cloud_storage.Contract;

import com.geekbrains.cloud_storage.GUI;

@FunctionalInterface
public interface RunnableGUI {
    public abstract void run(GUI gui);
}
