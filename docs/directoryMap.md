# Directory Map

This document explains the organization of files and folders in the Scheduled Smiles repository.

## Scheduled Smiles Directory Structure

```plaintext
Root Directory
├── frontend/                       # Main frontend source code for the application
│   ├── index.html                  # Default web page file for the project
│   ├── css/                        # css for styling the web page
│   └── assets/                     # any media that isn't code, especially images and videos
├── backend/                        # Main backend source code for the application
│   ├── Main.java                   # Entry point for the backend java program 
│   ├── User/                       # Package directory
│   │   └── Enum/                   # 
│   │       └── RoleConstant.java   #
│   ├── Server/                     # package directory
│   │   └── Enum/                   #
│   │       └── HttpConstants.java  #
│   └── lib/                        # Internal/external libraries used in this project
├── tests/                          # All test cases for the project
├── .gitignore                      # Specifies files and folders to be ignored by Git
│   ├── README.md                   # General overview of the project and usage examples
│   ├── LICENSE                     # The license under which the project is shared
│   └── docs/                       # Documentation files for the project, including API references and user guides
│       ├── API_REFERENCE.md        # Detailed information on the API for developers
│       ├── USER_GUIDE.md           # Step-by-step guide on using the application
│       └── PROJECT_GUIDELINE.md    # Detailde information on the project's requirements and planning
└── scripts/                        # Scripts for development and deployment
```



