# Historial de Versiones y Changelog (Hypixel Party Translator Mod)

## Versión 1.5.0 - "La Actualización Estética Visual Definitiva"

### ✨ Nuevas Características Integradas (Features)
1. **Reestructuración Completa de la Interfaz (Essential-Style UI):** Se eliminó por completo el motor de dibujado gris y anticuado de Minecraft Vanilla para las opciones (`Tecla V`). Se implementó un algoritmo propietario de cajas negras traslúcidas que reaccionan de manera inteligente bajo el cursor ("Hover"), simulando el diseño *Premium HUD* de modificaciones prestigiosas como "Essential".
2. **Introducción de Menús Desplegables (Nativos):** El ciclo de Idioma y la paleta de colores nacen como un "Dropdown System" codificado completamente a mano (Cero dependencias externas requeridas).
3. **Selector Estético de Colores Dinámicos:** Ahora es posible modificar de manera permanente el color del texto del recuadro traductor. Opciones de colores limpios: Blanco, Amarillo, Verde, Celeste, Rojo, Rosa, Naranja o Gris.
4. **Filtros Flexibles de Intercepción Duales (Global o Party):** Transición fluida con un solo clic entre un cerco seguro para traducir de forma pasiva únicamente el Chat interno de "Party", o apoderarse agresivamente de manera totalitaria de cualquier mensaje público, lobbies y gremios (Regex de intercepción dinámica).
5. **Transiciones Suaves Animadas en Mensajes Reales (Alpha Fading):** Inyectamos lógica milisegundo por milisegundo al recuadro de chat para recrear el "Fade-In" al nacer un mensaje y el "Fade-Out" suave y tenue de Minecraft Vanilla durante los últimos 500 ms de vida de la oración, borrándolo gradualmente en el aire.
6. **Background Dinámico de Renderizado:** La previsualización de bloques geométricos negros ya no está plagada de franjas por error de cálculo. Se estabilizó al encontrar un `maxWidth` de 320 px estricto copiando la resolución matriz del juego base de Mojang.

### 🐛 Resolución Estricta de Bugs
- Corregido un gravísimo defecto milimétrico con el uso de código Unicode y RGB Hexadecimal de formato `-1` donde los canales Alpha (Niveles de Opacidad) volvían el color 0xFFFFFF completamente invisible.
- Límite lógico per-línea estabilizado en 320 pixeles. Ahora los párrafos gigantes de los jugadores aplican automáticamente un *Word Wrapping*, saltando al renglón siguiente y respetando bordes. Mismos límites inyectados a la Caja Falsa del menú de configuración para una simetría 1:1.

---

## 🔧 Posibles Errores Comunes y Solución de Problemas (Troubleshooting)

Este Mod se ha codificado con alta retención de fallos, pero debido a la naturaleza extrema de intercepción a los flujos TCP de Hypixel usando peticiones HTTPS para evadir su protección, se pueden presentar ciertos síntomas.

#### ❌ Error 1: "Las letras jamás aparecen y el recuadro negro sí."
**Diagnóstico:** Puede deberse a dos factores principales: O el filtro Regex de asimilación falló en distinguir cómo le habla el jugador, O la conexión de tu PC generó un error de lectura por asincronismo en el servidor de Google.
**Solución Inmediata:** Presiona **[V]** y cambia el Modo Transición a `GLOBAL` e intenta hablar de nuevo.

#### ❌ Error 2: "Los botones translúcidos en el menú [V] no detectan mi Clic" o "El Desplegable se quedó atorado"
**Diagnóstico:** Se ha reescrito un manejador personal de áreas gráficas (Bounding Box hover detection). Si has redimensionado tu pantalla excesivamente mediante `F11` (Pantalla completa) fallando la relación de aspecto, el `mouseX` y `mouseY` podrían desalinearse unos milímetros de las pantallas.
**Solución Inmediata:** Reinicia la pestaña y presiona `Escape`. Si persiste, modifica tu relación de tamaño de Interfaz en `Opciones -> Gráficos -> Tamaño de Interfaz` dentro de las opciones de Minecraft vanilla momentáneamente.

#### ❌ Error 3: "Se me crashea Minecraft Inmediatamente al iniciar diciendo que hay un fallo de Mixins"
**Diagnóstico:** Interceptamos directamente una de las venas más delicadas y estrictas de Fabric `ChatHud.addMessage` mediante código de inyección Mixin para evadir bloqueos de Forge/Hypixel.
**Solución Inmediata:** Este fallo sucede **única y exclusivamente** si accidentalmente descargas este Mod e intentas correrlo en una versión futura o diferente a la de `Minecraft 1.21.1 / 1.21.11` (Donde los nombres del código de Mojang pueden cambiar). Utiliza siempre un perfil sin corromper de Fabric 1.21.

#### ❌ Error: La traducción funciona mal o devuelve el propio texto sin traducir luego de demorar"
**Diagnóstico:** El Helper de HttpClient está obteniendo lo que se denomina un Error `403 Forbidden` provocado por demasiadas solicitudes ("Rate Limiting").
**Solución Inmediata:** El mod posee un camuflaje potente de `User-Agent` Firefox y una ruta indirecta, ¡apágalo por 5 minutos y vuelve a intentarlo!

#### ❌ Error: Modificación de Tamaño no se guarda
**Diagnóstico:** Tu archivo local de memoria `party_translator.json` dentro de la carpeta `config` pudo corromperse o volverse de solo lectura (Windows Permissions).
**Solución Inmediata:** Ingresa a `.minecraft/config/` y borra manualmente el archivo `party_translator.json`. El mod lo regenerará virgen al volver a abrir la tecla V.
