package com.geekbrains.cs.client.Contract;

import com.geekbrains.cs.client.GUI;

@FunctionalInterface
public interface RunnableGUI {
    void run(GUI gui);
}
