package org.wso2.carbon.dataservices.expression;

/**
 * Created by rajith on 1/10/15.
 */
public interface ExpressionData {

    /**
     * method to get variable value. This method should be implemented in the object model
     *
     * @param varName will be the variable name it will be like var1.var2
     * @return variable value
     */
    Object getVariable(String varName);
}
