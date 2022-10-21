## Project Structure

### Submodules
- requestly-android: Top level module that need to be included as project dependency
- requestly-android-core: Module that contains all the core logic. Also contains all the submodules right now
- requestly-android-event: Submodule for handling analytics events related things. 
- requestly-android-okttp (requestly-network): Submodule for handling all the network related things.


### requestly-android-core
- src/main
    - java/io/requestly/android/core
        - modules: All the submodules until we split in multimodules 
        architecture.
            - logs: Submodule to capture `logs` from android app.
                - network: implements core network service class and abstract all the api calls for the submodule.
                - ui: Abstracts all the module specific UI.
            - hostSwitcher: Submodule to switch host from a network request.
            - okhttp: Move this here?? Maybe??
            - event: Move this here?? Maybe??
        - network: Contains all the common logic for making request to `api.requestly.io`
        - navigation: For handling navigation between submodules (Required in Future)
        - ui: Common UI (independent of submodules).
    - res
