/*
 * Copyright (C) 2014 Stefano Fornari.
 * All Rights Reserved.  No use, copying or distribution of this
 * work may be made except in accordance with a valid license
 * agreement from Stefano Fornari.  This notice must be
 * included on all copies, modifications and derivatives of this
 * work.
 *
 * STEFANO FORNARI MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. STEFANO FORNARI SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

/**
 * 
 */
Object.create = function(proto, props) {
    var obj = new Object();
    obj.__proto__ = proto;
    
    if (typeof props !== "undefined") {
        Object.defineProperties(obj, props);
    }
    
    return obj;
};

/**
 * Returns an array of strings representing all the enumerable property names of
 * the object.
 */
Object.keys = function(obj) {
    var array = new Array();
    for (var p in obj) {
        if (obj.hasOwnProperty(p)) {
            array.push(p);
        }
    }
    return array;
};

Object.getPrototypeOf = function(obj) {
    return obj.__proto__;
};

