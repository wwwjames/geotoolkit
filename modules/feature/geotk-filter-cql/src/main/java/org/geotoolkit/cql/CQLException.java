/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.cql;

import org.geotoolkit.util.Exceptions;

/**
 * CQL exception.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class CQLException extends Exception{

    public CQLException(String message) {
        super(message);
        assert Exceptions.isValidMessage(message) : message;
    }

    public CQLException(String message, Throwable cause) {
        super(message, cause);
        assert Exceptions.isValidMessage(message) : message;
    }
    
}
