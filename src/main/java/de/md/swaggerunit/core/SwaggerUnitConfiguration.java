// #***************************************************************************
// # mobilcom Vertrieb & Kunde Services - Source File: BaseTestConfiguration.java
// # Copyright (c) 1996-2018 by mobilcom-debitel GmbH
// # Author: mmalitz, Created on: 21.03.2018
// # All rights reserved.
// #***************************************************************************
package de.md.swaggerunit.core;

public interface SwaggerUnitConfiguration {

	public String getSwaggerSourceOverride();

	public String getSwaggerLoginUrl();

	public String getSwaggerLoginUsername();

	public String getSwaggerLoginPassword();
}
