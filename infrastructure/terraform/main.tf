resource "azurerm_resource_group" "devflow" {
  name     = var.resource_group_name
  location = var.location
}

resource "azurerm_container_registry" "devflow" {
  name                = "mehdihsb"
  resource_group_name = azurerm_resource_group.devflow.name
  location            = azurerm_resource_group.devflow.location
  sku                 = "Basic"
  admin_enabled       = false
}

resource "azurerm_service_plan" "devflow" {
  name                = "azure-cicd-plan"
  resource_group_name = azurerm_resource_group.devflow.name
  location            = azurerm_resource_group.devflow.location

  os_type  = "Linux"
  sku_name = "B1"
}

resource "azurerm_linux_web_app" "devflow" {
  name                = "azure-cicd-mehdi"
  resource_group_name = azurerm_resource_group.devflow.name
  location            = azurerm_resource_group.devflow.location
  service_plan_id     = azurerm_service_plan.devflow.id

  client_affinity_enabled = true

  ftp_publish_basic_authentication_enabled       = false
  webdeploy_publish_basic_authentication_enabled = false

  identity {
    type = "SystemAssigned"
  }

  app_settings = {
    "DB_URL"                              = "jdbc:postgresql://devflow-postgres.postgres.database.azure.com:5432/devflow?sslmode=require"
    "DB_USERNAME"                         = "devflowadmin"
    "DB_PASSWORD"                         = var.db_password
    "WEBSITES_ENABLE_APP_SERVICE_STORAGE" = "false"
    "WEBSITES_PORT"                       = "8081"
  }

  logs {
    detailed_error_messages = false
    failed_request_tracing  = false

    http_logs {
      file_system {
        retention_in_days = 3
        retention_in_mb   = 100
      }
    }
  }

  site_config {
    always_on = false

    container_registry_use_managed_identity = true

    ftps_state = "FtpsOnly"

    ip_restriction_default_action     = "Allow"
    scm_ip_restriction_default_action = "Allow"

    application_stack {
      docker_image_name   = "devflow-backend:latest"
      docker_registry_url = "https://mehdihsb.azurecr.io"
    }
  }

  lifecycle {
    ignore_changes = [
      app_settings["DB_PASSWORD"],
      site_config[0].ip_restriction_default_action,
      site_config[0].scm_ip_restriction_default_action,
      site_config[0].application_stack[0].docker_registry_username,
      site_config[0].application_stack[0].docker_registry_password
    ]
  }
}

resource "azurerm_postgresql_flexible_server" "devflow" {
  name                = "devflow-postgres"
  resource_group_name = azurerm_resource_group.devflow.name
  location            = azurerm_resource_group.devflow.location

  version                = "17"
  storage_mb             = 32768
  sku_name               = "B_Standard_B1ms"
  zone                   = "3"
  administrator_login    = "devflowadmin"
  administrator_password = var.db_password

  authentication {
    password_auth_enabled = true
  }

  lifecycle {
    ignore_changes = [
      administrator_password
    ]
  }
}
