variable "subscription_id" {
  description = "Azure subscription ID"
  type        = string
  sensitive   = true
}

variable "tenant_id" {
  description = "Azure tenant ID"
  type        = string
  sensitive   = true
}

variable "location" {
  description = "Azure region"
  type        = string
  default     = "spaincentral"
}

variable "resource_group_name" {
  description = "Azure resource group name"
  type        = string
  default     = "rg-cicd-spain"
}

variable "db_password" {
  description = "PostgreSQL administrator password"
  type        = string
  sensitive   = true
}
