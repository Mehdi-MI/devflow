output "resource_group_name" {
  description = "DevFlow Azure resource group"
  value       = azurerm_resource_group.devflow.name
}

output "acr_login_server" {
  description = "Azure Container Registry login server"
  value       = azurerm_container_registry.devflow.login_server
}

output "app_service_name" {
  description = "DevFlow App Service name"
  value       = azurerm_linux_web_app.devflow.name
}

output "app_service_url" {
  description = "DevFlow App Service URL"
  value       = "https://${azurerm_linux_web_app.devflow.default_hostname}"
}

output "app_service_principal_id" {
  description = "System-assigned managed identity principal ID"
  value       = azurerm_linux_web_app.devflow.identity[0].principal_id
}

output "jenkins_private_ip" {
  description = "Private IP address of the Jenkins VM"
  value       = azurerm_linux_virtual_machine.jenkins.private_ip_address
}

output "jenkins_public_ip" {
  description = "Public IP address of the Jenkins VM"
  value       = azurerm_linux_virtual_machine.jenkins.public_ip_address
}

output "jenkins_vm_name" {
  description = "Jenkins virtual machine name"
  value       = azurerm_linux_virtual_machine.jenkins.name
}
