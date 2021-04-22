# FinalProject

Final Project for COMP599 Introduction to Computer Animation at McGill University.

This project is a particle-based fluid simulation, using Smoothed Particle Hydrodynamics. This project is an implementation based on previous work done in this field of SPH.

## Running

This project can be run as a Java project directly. The control panel will have configuration options.
![image](https://github.com/hannahreed/ParticleBasedFluid/blob/main/images/controls.png)

* animate: toggles running the simulation
* Dimensions: sets the dimension of the system grid (NxN)
* stiffness: controls the stiffness of the liquid
* drop object: drops an optional object into the liquid
* viscosity coefficient for main liquid: main viscosity coefficient
* viscositiy coefficient for secondary liquid: if you want two liquids, this sets the viscosity coefficient of the second one
* particle factory: toggles an autogeneration of particles from the top left corner
* delay: time delay between generating a new particle for the particle factory
* tension coefficient: surface tension coefficient
* tension threshold: threshold for applying surface tension
* mass for main/secondary liquid: sets the masses
* two liquid types: toggles whether the system has one or two types of liquid

