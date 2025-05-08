# Swing Operations Documentation for MNI Users

This document provides details about the Swing Operations class and its functions, specifically for MNI users. Each function is described with its arguments and expected behavior.

## Functions

### `createWindow`
**Description:** Creates a new window with the specified title and dimensions.
- **Arguments:**
  - `title` (String): The title of the window.
  - `width` (Integer): The width of the window in pixels.
  - `height` (Integer): The height of the window in pixels.
- **Returns:** A handle (Integer) to the created window.

### `setWindowTitle`
**Description:** Sets the title of an existing window.
- **Arguments:**
  - `windowHandle` (Integer): The handle of the window.
  - `title` (String): The new title for the window.
- **Returns:** None.

### `addButton`
**Description:** Adds a button to a specified window.
- **Arguments:**
  - `windowHandle` (Integer): The handle of the window.
  - `label` (String): The text to display on the button.
  - `x` (Integer): The x-coordinate of the button.
  - `y` (Integer): The y-coordinate of the button.
  - `width` (Integer): The width of the button in pixels.
  - `height` (Integer): The height of the button in pixels.
- **Returns:** A handle (Integer) to the created button.

### `setButtonAction`
**Description:** Assigns an action to a button.
- **Arguments:**
  - `buttonHandle` (Integer): The handle of the button.
  - `action` (String): The action to perform when the button is clicked.
- **Returns:** None.

### `showWindow`
**Description:** Displays a window on the screen.
- **Arguments:**
  - `windowHandle` (Integer): The handle of the window.
- **Returns:** None.

### `closeWindow`
**Description:** Closes a specified window.
- **Arguments:**
  - `windowHandle` (Integer): The handle of the window.
- **Returns:** None.

### `addLabel`
**Description:** Adds a label to a specified window.
- **Arguments:**
  - `windowHandle` (Integer): The handle of the window.
  - `text` (String): The text to display on the label.
  - `x` (Integer): The x-coordinate of the label.
  - `y` (Integer): The y-coordinate of the label.
- **Returns:** A handle (Integer) to the created label.

### `updateLabel`
**Description:** Updates the text of an existing label.
- **Arguments:**
  - `labelHandle` (Integer): The handle of the label.
  - `text` (String): The new text for the label.
- **Returns:** None.

### `addTextField`
**Description:** Adds a text field to a specified window.
- **Arguments:**
  - `windowHandle` (Integer): The handle of the window.
  - `x` (Integer): The x-coordinate of the text field.
  - `y` (Integer): The y-coordinate of the text field.
  - `width` (Integer): The width of the text field in pixels.
  - `height` (Integer): The height of the text field in pixels.
- **Returns:** A handle (Integer) to the created text field.

### `getTextFieldValue`
**Description:** Retrieves the current value of a text field.
- **Arguments:**
  - `textFieldHandle` (Integer): The handle of the text field.
- **Returns:** The current value (String) of the text field.

### `setTextFieldValue`
**Description:** Sets the value of a text field.
- **Arguments:**
  - `textFieldHandle` (Integer): The handle of the text field.
  - `value` (String): The value to set in the text field.
- **Returns:** None.