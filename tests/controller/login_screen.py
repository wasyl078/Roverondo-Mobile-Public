from time import sleep as wait
from selenium.webdriver.common.by import By
from controller.abstract_controller import AbstractController


class LoginScreen(AbstractController):
    def __init__(self, driver):
        super().__init__(driver)

        # Locators
        self.allow_location_button = (By.ID,
                                      "com.android.permissioncontroller:id/permission_allow_foreground_only_button")
        self.continue_authorization_button = (
            By.ID, "com.example.roverondo_mobile:id/loginTextView")
        self.edit_text_view = (By.CLASS_NAME, "android.widget.EditText")
        self.button = (By.CLASS_NAME, "android.widget.Button")
        self.image_button = (By.CLASS_NAME, "android.widget.ImageButton")
        self.wall_text = (By.XPATH, "//*[text()='Wall']")

    def allow_access_to_location(self):
        return self.click_button(self.allow_location_button)

    def click_continue_authorization(self):
        return self.click_button(self.continue_authorization_button)

    def login_to_auth0(self):
        if not self.wait_for(*self.edit_text_view):
            return

        username_input, password_input = self.driver.find_elements(
            *self.edit_text_view)[:2]

        username_input.send_keys("cokowo9977@ningame.com")
        password_input.send_keys("u7DTmJy3!")

        assert self.click_button(self.button, idx=1)
        wait(5)

    def get_continue_authorization_text(self):
        return self.get_text(self.continue_authorization_button)
