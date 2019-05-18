package com.geekbrains.cs.client.Contracts;

import com.geekbrains.cs.client.GUI;

@FunctionalInterface
public interface RunnableGUI {
    void run(GUI gui);
}
