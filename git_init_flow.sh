#!/bin/bash

# Script de inicialización de flujo de trabajo Git (Git Flow) con Conventional Commits en Español

echo "=========================================================="
echo "Inicializando repositorio Git para Sudoku Offline..."
echo "=========================================================="

# 1. Inicializar repositorio git
git init

# 2. Configurar la rama inicial por defecto como main
git checkout -b main

# 3. Agregar todos los archivos creados
git add .

# 4. Primer commit en la rama main
git commit -m "feat: inicializar estructura de proyecto con Docker, Room y Compose"

# 5. Crear la rama de desarrollo (develop) a partir de main
git checkout -b develop

echo "=========================================================="
echo "¡Repositorio inicializado con éxito!"
echo "Ramas creadas:"
echo "  - main    (producción)"
echo "  - develop (desarrollo activo)"
echo ""
echo "Para trabajar en nuevas características, utiliza Git Flow:"
echo "  1. Crear rama:   git checkout -b feature/mi-nueva-caracteristica develop"
echo "  2. Hacer commits: git commit -m \"feat: agregar componente X\""
echo "  3. Fusiones:     git checkout develop && git merge feature/mi-nueva-caracteristica"
echo "=========================================================="
