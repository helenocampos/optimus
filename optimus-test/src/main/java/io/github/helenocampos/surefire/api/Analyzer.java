/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.helenocampos.surefire.api;

import io.github.helenocampos.surefire.AbstractTest;

/**
 *
 * @author helenocampos
 */
public interface Analyzer
{
    public float getTestScore(AbstractTest test, String... arguments);
}
