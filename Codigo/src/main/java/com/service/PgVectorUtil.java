package com.service;

/**
 * Utilitario para transformar um float[] em literal do tipo pgvector: [v1,v2,...,vn]
 * (Esse literal pode ser setado em um campo do tipo public.vector no PostgreSQL.)
 */
public class PgVectorUtil
{
    /**
     * Converte um vetor de floats em uma string no formato aceito pelo pgvector.
     * @param v vetor de floats do embedding.
     * @return string com formato "[1.23,0.45,...]".
     */
    public static String toPgVectorLiteral(float[] v)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < v.length; i = i + 1)
        {
            if (i > 0)
            {
                sb.append(',');
            }
            sb.append(v[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
