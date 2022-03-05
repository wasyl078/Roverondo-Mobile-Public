import pytest
from controller.tools_controller import ToolsAppium


@pytest.fixture()
def Tools():
    appium = ToolsAppium()
    yield appium
    appium.shut_down()
