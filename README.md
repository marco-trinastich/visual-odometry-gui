# VisualOdometry GUI

VisualOdometry GUI is a graphical user interface application for a **Tracking and Mapping System based on Visual Odometry**. It is a Java-based application that leverages the **BoofCV library** for computer vision tasks, enabling users to implement and experiment with various visual odometry algorithms. The application processes input from **video feeds** or **live cameras** and generates a **2D map** as an output, providing a comprehensive platform for visual odometry research and development.

## Key Features

### Visual Odometry Algorithms

- Supports multiple types of visual odometry:
  - **MonoPlaneInfinity**
  - **MonoPlaneOverhead**
  - Placeholders for stereo and depth-based odometry types.
- Algorithms are created using the `VisualOdometryFactory`.

### GUI Components

- Panels for configuring visual odometry settings:
  - `MonoPlaneOverheadPanel`
  - `MonoPlaneInfinityPanel`
- Toolbar buttons for:
  - Starting, pausing, stopping, resetting, and clearing visual odometry processes.
- Info panels for displaying processing details such as:
  - Inliers, tracks, and status.

### Input Support

- Supports input from:
  - **Video files** for offline processing.
  - **Live camera feeds** for real-time visual odometry.

### Processing Core

- The `Core` class manages visual odometry processing, including:
  - Setup, execution, and error handling.
  - Timed processing and thread-safe state management.

### Settings Management

- Configuration is stored in XML files (`settings.xml`), which define:
  - Parameters for trackers.
  - Visual odometry types.
  - Calibration files.

### Integration

- Uses **BoofCV libraries** for visual odometry and tracking.
- Includes dependencies for:
  - Image processing.
  - Calibration.
  - Visualization.

### Error Handling

- Displays error dialogs for invalid configurations or processing failures.

## Technologies Used

- **Java**: Core programming language.
- **BoofCV**: For visual odometry and computer vision tasks.
- **Quarkus**: Framework for application lifecycle management.
- **Swing**: For GUI components.
- **Lombok**: For reducing boilerplate code.
- **Jakarta CDI**: For dependency injection.

## Purpose

This project is intended for research or development in visual odometry, enabling users to experiment with different algorithms and configurations through an interactive GUI.

## Getting Started

### Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher.
- **Apache Maven**: For building and managing the project.

### Building the Project

To build the project, run the following command in the terminal:

```bash
mvn clean install
```

### Running the Application

After building the project, you can run the application using:

```bash
java -jar target/vogui-1.0.0.jar
```

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for more details.

## Contributing

Contributions are welcome! If you'd like to contribute:

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Submit a pull request with a detailed description of your changes.

## Contact

For any inquiries or support, please contact the project maintainers.
