package com.geekbrains.cs.client.Contract;

import com.geekbrains.cs.client.GUI;

@FunctionalInterface
public interface RunnableGUI {
    public abstract void run(GUI gui);
}
