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

# ============================================================
# Jenkins Infrastructure
# ============================================================

resource "azurerm_virtual_network" "jenkins" {
  name                = "devflow-jenkins-vnet"
  location            = azurerm_resource_group.devflow.location
  resource_group_name = azurerm_resource_group.devflow.name
  address_space       = ["10.20.0.0/16"]
}

resource "azurerm_subnet" "jenkins" {
  name                 = "jenkins-subnet"
  resource_group_name  = azurerm_resource_group.devflow.name
  virtual_network_name = azurerm_virtual_network.jenkins.name
  address_prefixes     = ["10.20.1.0/24"]
}

resource "azurerm_public_ip" "jenkins" {
  name                = "devflow-jenkins-ip"
  location            = azurerm_resource_group.devflow.location
  resource_group_name = azurerm_resource_group.devflow.name

  allocation_method = "Static"
  sku               = "Standard"
}

resource "azurerm_network_security_group" "jenkins" {
  name                = "devflow-jenkins-nsg"
  location            = azurerm_resource_group.devflow.location
  resource_group_name = azurerm_resource_group.devflow.name

  security_rule {
    name                       = "Allow-SSH-From-Admin"
    priority                   = 100
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "22"
    source_address_prefix      = "41.103.96.94/32"
    destination_address_prefix = "*"
  }

  security_rule {
    name                       = "Allow-Jenkins-Local"
    priority                   = 110
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "8080"
    source_address_prefix      = "VirtualNetwork"
    destination_address_prefix = "*"
  }

  security_rule {
    name                       = "Deny-Internet-Inbound"
    priority                   = 4000
    direction                  = "Inbound"
    access                     = "Deny"
    protocol                   = "*"
    source_port_range          = "*"
    destination_port_range     = "*"
    source_address_prefix      = "Internet"
    destination_address_prefix = "*"
  }
}

resource "azurerm_network_interface" "jenkins" {
  name                = "devflow-jenkins-nic"
  location            = azurerm_resource_group.devflow.location
  resource_group_name = azurerm_resource_group.devflow.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.jenkins.id
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.jenkins.id
  }
}

resource "azurerm_network_interface_security_group_association" "jenkins" {
  network_interface_id      = azurerm_network_interface.jenkins.id
  network_security_group_id = azurerm_network_security_group.jenkins.id
}

resource "azurerm_linux_virtual_machine" "jenkins" {
  name                = "devflow-jenkins"
  resource_group_name = azurerm_resource_group.devflow.name
  location            = azurerm_resource_group.devflow.location
  size                = "Standard_B2ls_v2"

  admin_username                  = "devflow"
  disable_password_authentication = true

  network_interface_ids = [
    azurerm_network_interface.jenkins.id
  ]

  admin_ssh_key {
    username   = "devflow"
    public_key = file(pathexpand("~/.ssh/id_ed25519.pub"))
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
    disk_size_gb         = 30
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "ubuntu-24_04-lts"
    sku       = "server"
    version   = "latest"
  }

  custom_data = filebase64("${path.module}/jenkins-cloud-init.sh")
}
