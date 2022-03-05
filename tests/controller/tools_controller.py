import subprocess as sp
from time import sleep as wait

from appium import webdriver
from controller.abstract_controller import AbstractController
from controller.login_screen import LoginScreen
from controller.wall_screen import WallScreen
from controller.leaderboards_screen import LeaderboardsScreen
from controller.user_screen import UserScreen
from controller.post_screen import PostScreen


class ToolsAppium(AbstractController):

    def __init__(self):
        # Set up
        self.init_server()
        super().__init__(self.driver)

        self.login = LoginScreen(self.driver)
        self.wall = WallScreen(self.driver)
        self.leaderboards = LeaderboardsScreen(self.driver)
        self.user = UserScreen(self.driver)
        self.post = PostScreen(self.driver)

    # Utils - Set Up
    def init_server(self):
        sp.Popen("appium", shell=True, stdout=sp.DEVNULL, stderr=sp.DEVNULL)
        wait(5)
        caps = {
            "platformName": "Android", "platformVersion": "",
            "deviceName": "emulator-5554",
            "appPackage": "com.example.roverondo_mobile",
            "appActivity": ".activities.LoginActivity"
        }
        self.driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", caps)

    # Utils - Shut Down
    def shut_down(self):
        self.driver.quit()

    # Utils scroll
    def scroll_top_bar_down(self):
        wait(2)
        self.driver.swipe(20, 20, 20, 500, 400)
        return True

    def scroll_top_bar_up(self):
        wait(2)
        self.driver.swipe(20, 300, 20, 20, 400)
        return True

    def slide_down(self, repeats=1):
        for _ in range(repeats):
            wait(2)
            self.driver.swipe(300, 1200, 300, 200, 500)
        wait(2)
        return True

    def scroll_down(self):
        wait(2)
        self.driver.swipe(470, 1400, 470, 300, 400)
        return True

    # Utils general
    def click_back_button(self):
        wait(2)
        self.driver.press_keycode(4)

    def click_home_button(self):
        wait(2)
        self.driver.press_keycode(3)

    def click_recent_apps_button(self):
        wait(2)
        self.driver.press_keycode(187)
