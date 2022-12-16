// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.placesdemo.model

import java.io.Serializable

/** A Plus Code encoded location reference.  */
class PlusCode : Serializable {
    /** The global Plus Code identifier.  */
    var globalCode: String? = null

    /** The compound Plus Code identifier. May be null for locations in remote areas.  */
    var compoundCode: String? = null

    override fun toString(): String {
        val sb = StringBuilder("[PlusCode: ")
        sb.append(globalCode)
        if (compoundCode != null) {
            sb.append(", compoundCode=").append(compoundCode)
        }
        sb.append("]")
        return sb.toString()
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}